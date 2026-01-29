package com.team44.isa_youtubeich.service.impl;

import com.team44.isa_youtubeich.domain.model.Video;
import com.team44.isa_youtubeich.domain.model.VideoStatus;
import com.team44.isa_youtubeich.repository.VideoRepository;
import com.team44.isa_youtubeich.service.LivestreamService;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
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

    private static final String HLS_DIR = "uploads/hls/";

    private final ConcurrentHashMap<Long, Process> runningProcesses = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();

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

        // Stop transcoding if running
        Process process = runningProcesses.remove(videoId);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }

        cleanupHlsOutput(videoId);
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

        // Stop transcoding if running
        Process process = runningProcesses.remove(videoId);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }

        cleanupHlsOutput(videoId);
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
        try {
            Path hlsPath = Paths.get(HLS_DIR + video.getId());
            Files.createDirectories(hlsPath);

            Process process = runFfmpeg(video, hlsPath);
            runningProcesses.put(video.getId(), process);

            // Drain ffmpeg output so the process can never block due to a full stdout buffer.
            startLogDrainer(video.getId(), process);

            // Wait for process to finish and end premiere
            new Thread(() -> {
                try {
                    int exitCode = process.waitFor();
                    runningProcesses.remove(video.getId());

                    // If the process was killed intentionally (endPremiere/cancel), don't mark it again.
                    if (exitCode == 0) {
                        endPremiere(video.getId());
                    } else {
                        // 137 is a common "killed" exit code in containers; 255 is also common for forced stop.
                        if (exitCode == 137 || exitCode == 255) {
                            log.info("ffmpeg stopped (likely terminated) for videoId={} exitCode={}", video.getId(), exitCode);
                        } else {
                            log.warn("ffmpeg exited with code {} for videoId={}", exitCode, video.getId());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "ffmpeg-wait-" + video.getId()).start();

        } catch (IOException e) {
            log.error("Failed to start HLS transcoding for videoId={}", video.getId(), e);
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

    @PostConstruct
    void resumePremieresOnStartup() {
        // Assumption: a premiere should be considered "live" if premieresAt is in the past (or now)
        // and status is SCHEDULED or LIVE.
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        for (Video video : videoRepository.findPremieresToResume()) {
            if (video.getPremieresAt() == null) continue;

            // If it's already in our in-memory map, don't start another ffmpeg.
            if (runningProcesses.containsKey(video.getId())) continue;

            if (!video.getPremieresAt().isAfter(now)) {
                // We should be live now. Ensure status is LIVE and start transcoding.
                if (video.getStatus() == VideoStatus.SCHEDULED) {
                    video.setStatus(VideoStatus.LIVE);
                    videoRepository.save(video);
                }

                log.info("Resuming/starting premiere stream on startup for videoId={} premieresAt={} status={}",
                        video.getId(), video.getPremieresAt(), video.getStatus());
                transcodeToHLS(video);
            } else {
                // Premiere is still in the future, but the schedule might be missing if we restarted.
                // Re-schedule local task (and publish to Redis so other instances can schedule too).
                Duration until = Duration.between(now, video.getPremieresAt());
                log.info("Re-scheduling future premiere on startup for videoId={} premieresAt={} (in {}s)",
                        video.getId(), video.getPremieresAt(), until.getSeconds());
                schedulePremiere(video.getId(), video.getPremieresAt());
            }
        }
    }
}

