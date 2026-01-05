package com.team44.isa_youtubeich.exception;

/**
 * Exception thrown when a user attempts to login with an account that has not been activated.
 * This exception extends NonOpaqueException to ensure the activation message is always displayed
 * to the user, even in production mode.
 */
public class AccountNotActivatedException extends NonOpaqueException {
    public AccountNotActivatedException() {
        super("Account is not activated. Please check your email for the activation link.");
    }

    public AccountNotActivatedException(String message) {
        super(message);
    }
}

