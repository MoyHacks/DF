package org.coppel.omnicanal.exceptions;


public class TransientHttpException extends RuntimeException {
    public TransientHttpException(String message) {
        super(message);
    }
}
