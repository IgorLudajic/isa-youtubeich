package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.domain.model.VideoStatus;
import com.team44.isa_youtubeich.instance.RedisVideoTranscodeLeaseService;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.LivestreamService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class LivestreamServiceImpl implements LivestreamService {

    private static final Logger log = LoggerFactory.getLogger(LivestreamServiceImpl.class);

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private RedisVideoTranscodeLeaseService transcodeLeaseService;

    private static final String HLS_DIR = "uploads/hls/";

    private final ConcurrentHashMap<Long, Process> runningProcesses = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> leaseHeartbeats = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledCleanups = new ConcurrentHashMap<>();

    private static final Duration HLS_CLEANUP_DELAY = Duration.ofMinutes(1);

    @Override
    public String getHlsDirectory() { return HLS_DIR; }

    @Override
    public void schedulePremiere(Long videoId, LocalDateTime premieresAt) {
        // Schedule on current instance
        ScheduledFuture<?> future = taskScheduler.schedule(() -> startPremiere(videoId), premieresAt.atZone(ZoneOffset.UTC).toInstant());
        scheduledFutures.put(videoId, future);
    }

    @Override
    public void startPremiere(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow();

        // Ensure only one instance actually does the transcoding. Others can still flip status,
        // but only the lease-holder runs ffmpeg and owns lifecycle actions.
        if (!transcodeLeaseService.tryAcquire(videoId)) {
            log.info("Skipping startPremiere for videoId={} (transcoding lease held by another instance)", videoId);
            return;
        }

        // If we previously scheduled a cleanup (due to end), cancel it because we're starting again.
        cancelCleanup(videoId);

        if (video.getStatus() == VideoStatus.SCHEDULED) {
            video.setStatus(VideoStatus.LIVE);
            videoRepository.save(video);
        }

        // Start transcoding
        transcodeToHLS(video);
    }

    @Override
    public void endPremiere(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow();

        video.setStatus(VideoStatus.ENDED);
        videoRepository.save(video);

        stopFfmpegIfRunning(videoId);

        // Release lease immediately so another instance can take over if needed.
        stopLeaseHeartbeat(videoId);
        transcodeLeaseService.releaseIfHeld(videoId);

        // Keep HLS around briefly for late fetches, then cleanup.
        scheduleCleanup(videoId);
    }

    @Override
    public void cancelPremiere(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow();

        // Mark as ended ("cancelled" is treated as ended in current domain model)
        video.setStatus(VideoStatus.ENDED);
        videoRepository.save(video);

        // Cancel scheduled task if it exists
        ScheduledFuture<?> future = scheduledFutures.remove(videoId);
        if (future != null) {
            future.cancel(false);
        }

        stopFfmpegIfRunning(videoId);

        stopLeaseHeartbeat(videoId);
        transcodeLeaseService.releaseIfHeld(videoId);

        scheduleCleanup(videoId);
    }

    @Override
    public void startPremiereEarly(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow();
        if (video.getStatus() != VideoStatus.SCHEDULED) return;

        // Cancel scheduled task
        ScheduledFuture<?> future = scheduledFutures.remove(videoId);
        if (future != null) {
            future.cancel(false);
        }

        // Start immediately
        startPremiere(videoId);
    }

    private void transcodeToHLS(Video video) {
        long videoId = video.getId();
        try {
            // Lease is required here too (defense in depth).
            if (!transcodeLeaseService.isHeldByMe(videoId)) {
                log.info("Not starting ffmpeg for videoId={} because this instance does not hold the lease", videoId);
                return;
            }

            // Cancel any pending cleanup from a previous end.
            cancelCleanup(videoId);

            Path hlsPath = Paths.get(HLS_DIR + videoId);
            Files.createDirectories(hlsPath);

            // Best-effort: if a previous instance died mid-stream, there might be old segments.
            // We allow ffmpeg to append to playlist (-hls_flags append_list). If it fails, we can
            // still restart from scratch by cleaning the directory manually.

            Process process = runFfmpeg(video, hlsPath);
            runningProcesses.put(videoId, process);

            // Heartbeat the lease while ffmpeg is alive.
            startLeaseHeartbeat(videoId);

            // Drain ffmpeg output so the process can never block due to a full stdout buffer.
            startLogDrainer(videoId, process);

            // Wait for process to finish and end premiere
            new Thread(() -> {
                try {
                    int exitCode = process.waitFor();
                    runningProcesses.remove(videoId);

                    // Stop heartbeat and release lease when ffmpeg exits.
                    stopLeaseHeartbeat(videoId);
                    transcodeLeaseService.releaseIfHeld(videoId);

                    // If the process finished normally, end premiere.
                    if (exitCode == 0) {
                        endPremiere(videoId);
                    } else {
                        // 137 is a common "killed" exit code in containers; 255 is also common for forced stop.
                        if (exitCode == 137 || exitCode == 255) {
                            log.info("ffmpeg stopped (likely terminated) for videoId={} exitCode={}", videoId, exitCode);
                        } else {
                            log.warn("ffmpeg exited with code {} for videoId={}", exitCode, videoId);
                        }
                        // Don't endPremiere automatically on abnormal exit; rely on resume logic / manual actions.
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "ffmpeg-wait-" + videoId).start();

        } catch (IOException e) {
            log.error("Failed to start HLS transcoding for videoId={}", videoId, e);
            // Ensure we don't keep a stale lease if ffmpeg failed to start.
            stopLeaseHeartbeat(videoId);
            transcodeLeaseService.releaseIfHeld(videoId);
        }
    }

    private void startLogDrainer(Long videoId, Process process) {
        Thread t = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Keep this at debug so we don't spam prod logs.
                    log.debug("ffmpeg[{}]: {}", videoId, line);
                }
            } catch (IOException ignored) {
                // Process is exiting / stream closed.
            }
        }, "ffmpeg-log-" + videoId);
        t.setDaemon(true);
        t.start();
    }

    private static @NonNull Process runFfmpeg(Video video, Path hlsPath) throws IOException {
        String inputPath = video.getVideoUrl();
        String outputPath = hlsPath.toString() + "/playlist.m3u8";

        // NOTE:
        // - "Premiere" here is a VOD file presented as a live stream.
        // - "-re" makes ffmpeg read the input at native/real-time speed, so it won't finish instantly.
        // - We keep a small sliding window playlist and delete older segments to reduce rewind.
        //
        // Important: by default ffmpeg writes relative segment URIs into the playlist.
        // Our REST "/api/videos/{id}/stream" endpoint returns the playlist bytes, but it won't serve
        // the individual .ts segment files. We therefore bake an absolute (server) base URL into the
        // playlist so players fetch segments from the static /uploads/** mapping.
        String segmentBaseUrl = "/uploads/hls/" + video.getId() + "/";

        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-hide_banner",
            "-loglevel", "warning",
            "-re",
            "-i", inputPath,
            "-c:v", "libx264",
            "-preset", "veryfast",
            "-tune", "zerolatency",
            // Encourage encoder to emit frequent keyframes so short segments are decodable.
            // With hls_time=0.5, target ~2 keyframes per second.
            "-g", "30",
            "-keyint_min", "30",
            "-sc_threshold", "0",
            "-c:a", "aac",
            "-ar", "48000",
            "-b:a", "128k",
            // Lower-latency HLS tuning:
            // - shorter segments
            // - smaller playlist window
            // - independent segments for clean switching
            // - delete old segments to avoid unbounded disk usage
            "-hls_time", "0.5",
            "-hls_list_size", "4",
            "-hls_flags", "delete_segments+append_list+independent_segments+omit_endlist+program_date_time",
            "-hls_delete_threshold", "1",
            // Add URL prefix into playlist so clients fetch segments from our static mapping.
            "-hls_base_url", segmentBaseUrl,
            "-hls_segment_filename", hlsPath + "/segment_%05d.ts",
            "-f", "hls",
            outputPath
        );

        // Merge stderr into stdout so we can drain a single stream.
        pb.redirectErrorStream(true);
        return pb.start();
    }

    private void cleanupHlsOutput(Long videoId) {
        Path dir = Paths.get(HLS_DIR + videoId);
        if (!Files.exists(dir)) return;

        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public @NonNull FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public @NonNull FileVisitResult postVisitDirectory(@NonNull Path directory, IOException exc) throws IOException {
                    Files.deleteIfExists(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("Failed to cleanup HLS output for videoId={} at {}", videoId, dir, e);
        }
    }

    private void startLeaseHeartbeat(long videoId) {
        // If already scheduled, don't double-heartbeat.
        if (leaseHeartbeats.containsKey(videoId)) return;

        // Tight heartbeat: ~1/3 TTL.
        long periodMs = Math.max(1000, transcodeLeaseService.getLeaseTtl().toMillis() / 3);

        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                // If we lost the lease, stop ffmpeg so another instance can take over quickly.
                boolean renewed = transcodeLeaseService.renewIfHeld(videoId);
                if (!renewed) {
                    log.warn("Lost transcoding lease for videoId={} - stopping local ffmpeg", videoId);
                    stopFfmpegIfRunning(videoId);
                    stopLeaseHeartbeat(videoId);
                }
            } catch (Exception e) {
                // If Redis is temporarily unavailable, keep ffmpeg running; lease will expire and takeover can occur.
                log.debug("Lease heartbeat error for videoId={}", videoId, e);
            }
        }, Duration.ofMillis(periodMs));

        leaseHeartbeats.put(videoId, future);
    }

    private void stopLeaseHeartbeat(long videoId) {
        ScheduledFuture<?> fut = leaseHeartbeats.remove(videoId);
        if (fut != null) {
            fut.cancel(false);
        }
    }

    private void stopFfmpegIfRunning(long videoId) {
        Process process = runningProcesses.remove(videoId);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private void scheduleCleanup(long videoId) {
        cancelCleanup(videoId);

        ScheduledFuture<?> fut = taskScheduler.schedule(() -> {
            try {
                // If someone restarted the premiere and re-acquired lease, skip deletion.
                if (transcodeLeaseService.isLeaseActive(videoId)) {
                    log.info("Skipping HLS cleanup for videoId={} because transcode lease is active", videoId);
                    return;
                }
                cleanupHlsOutput(videoId);
            } finally {
                scheduledCleanups.remove(videoId);
            }
        }, Instant.now().plus(HLS_CLEANUP_DELAY));

        scheduledCleanups.put(videoId, fut);
    }

    private void cancelCleanup(long videoId) {
        ScheduledFuture<?> fut = scheduledCleanups.remove(videoId);
        if (fut != null) {
            fut.cancel(false);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    void resumePremieresOnStartup() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        for (Video video : videoRepository.findPremieresToResume()) {
            if (video.getPremieresAt() == null) continue;

            // If it's already in our in-memory map, don't start another ffmpeg.
            if (runningProcesses.containsKey(video.getId())) continue;

            if (!video.getPremieresAt().isAfter(now)) {
                // We should be live now.
                // Only lease-holder should start ffmpeg; others just log and move on.
                if (transcodeLeaseService.tryAcquire(video.getId())) {
                    cancelCleanup(video.getId());
                    if (video.getStatus() == VideoStatus.SCHEDULED) {
                        video.setStatus(VideoStatus.LIVE);
                        videoRepository.save(video);
                    }

                    log.info("Resuming/starting premiere stream on startup for videoId={} premieresAt={} status={}",
                            video.getId(), video.getPremieresAt(), video.getStatus());
                    transcodeToHLS(video);
                } else {
                    log.info("Not resuming videoId={} on this instance (lease held elsewhere)", video.getId());
                }
            } else {
                // Premiere is still in the future, but the schedule might be missing if we restarted.
                Duration until = Duration.between(now, video.getPremieresAt());
                log.info("Re-scheduling future premiere on startup for videoId={} premieresAt={} (in {}s)",
                        video.getId(), video.getPremieresAt(), until.getSeconds());
                schedulePremiere(video.getId(), video.getPremieresAt());
            }
        }
    }
}

