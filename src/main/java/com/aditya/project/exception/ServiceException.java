package com.aditya.project.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private final ErrorCatalog errorCatalog;

    public ServiceException(ErrorCatalog errorCatalog, Throwable throwable) {
        super(throwable);
        this.errorCatalog = errorCatalog;
    }
}
