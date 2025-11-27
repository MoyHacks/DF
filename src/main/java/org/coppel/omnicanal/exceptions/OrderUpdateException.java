package org.coppel.omnicanal.exceptions;

import lombok.Getter;

@Getter
public class OrderUpdateException extends RuntimeException {

    private final Long customerOrderId;
    private final ErrorType type;
    private final String errorCode;

    public enum ErrorType {
        CONNECTION,
        UNEXPECTED,
        SECRET_MANAGER,
        TOKEN_PARSE,
        CATALOG_LOAD,
        PIPELINE_INIT
    }

    public OrderUpdateException(String message, Long customerOrderId, ErrorType type, Throwable cause) {
        this(message, customerOrderId, type, null, cause);
    }

    public OrderUpdateException(String message, Long customerOrderId, ErrorType type, String errorCode, Throwable cause) {
        super(message, cause);
        this.customerOrderId = customerOrderId;
        this.type = type;
        this.errorCode = errorCode;
    }


    @Override
    public String toString() {
        return "OrderUpdateException{" +
                "type=" + type +
                ", errorCode='" + errorCode + '\'' +
                ", customerOrderId='" + customerOrderId + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
