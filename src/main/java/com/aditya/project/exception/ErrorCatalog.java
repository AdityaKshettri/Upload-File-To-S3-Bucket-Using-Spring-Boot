package com.aditya.project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCatalog {

    APPLICATION_ERROR(1, "Application Error occurred!!", ErrorLevel.FUNCTIONAL),
    INTERRUPTED_ERROR(1, "Process Interrupted error!!", ErrorLevel.TECHNICAL),
    S3_CONNECTION__ERROR(1, "Error connecting to S3!!", ErrorLevel.TECHNICAL);

    private final int code;
    private final String message;
    private final ErrorLevel errorLevel;
}
