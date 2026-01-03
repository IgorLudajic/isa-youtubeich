package com.team44.isa_youtubeich.exception;

import lombok.Getter;
import lombok.Setter;

public class ResourceConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    @Getter @Setter private Long resourceId;

    public ResourceConflictException(Long resourceId, String message) {
        super(message);
        this.setResourceId(resourceId);
    }
}