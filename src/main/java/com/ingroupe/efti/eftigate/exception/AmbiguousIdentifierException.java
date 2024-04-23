package com.ingroupe.efti.eftigate.exception;

public class AmbiguousIdentifierException extends RuntimeException{
    public AmbiguousIdentifierException(String message) {
        super(message);
    }

    public AmbiguousIdentifierException(Throwable cause) {
        super(cause);
    }

    public AmbiguousIdentifierException(String message, Throwable cause) {
        super(message, cause);
    }
}
