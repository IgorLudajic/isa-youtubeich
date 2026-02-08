package com.team44.isa_youtubeich.dto;

import com.team44.isa_youtubeich.domain.model.VideoStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Service-level result for deciding what a client should stream for a given video.
 * Controller can translate this into HTTP (e.g. 302 redirect / 404 / 409).
 */
@Setter
@Getter
public class VideoStreamResolutionDto {

    public enum StreamKind {
        HLS,
        VOD
    }

    public enum Availability {
        AVAILABLE,
        NOT_READY,
        MISSING
    }

    private Availability availability;
    private StreamKind kind;
    private String location; // relative URL under our server, e.g. "/uploads/hls/{id}/playlist.m3u8"

    private VideoStatus videoStatus;
    private String message;

    public VideoStreamResolutionDto() {
    }

    public VideoStreamResolutionDto(Availability availability, StreamKind kind, String location, VideoStatus videoStatus, String message) {
        this.availability = availability;
        this.kind = kind;
        this.location = location;
        this.videoStatus = videoStatus;
        this.message = message;
    }

    public static VideoStreamResolutionDto available(StreamKind kind, String location, VideoStatus status) {
        return new VideoStreamResolutionDto(Availability.AVAILABLE, kind, location, status, null);
    }

    public static VideoStreamResolutionDto notReady(VideoStatus status, String message) {
        return new VideoStreamResolutionDto(Availability.NOT_READY, null, null, status, message);
    }

    public static VideoStreamResolutionDto missing(VideoStatus status, String message) {
        return new VideoStreamResolutionDto(Availability.MISSING, null, null, status, message);
    }

}
