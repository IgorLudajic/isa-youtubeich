package com.team44.isa_youtubeich.exception;

/**
 * Base exception class for exceptions that should always display their detailed message,
 * bypassing the opaque error filter even in production mode.
 * <p>
 * This should be used for exceptions where the detailed message is safe to expose
 * and provides necessary user guidance (e.g., account activation instructions).
 */
public abstract class NonOpaqueException extends RuntimeException {
    public NonOpaqueException(String message) {
        super(message);
    }

    public NonOpaqueException(String message, Throwable cause) {
        super(message, cause);
    }
}

