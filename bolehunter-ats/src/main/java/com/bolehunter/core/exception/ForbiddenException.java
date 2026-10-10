package com.bolehunter.core.exception;

public class ForbiddenException extends HttpException {

    public ForbiddenException(int code) {
        this.code = code;
        this.httpStatusCode = 407;
    }

    public ForbiddenException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 407;
        this.message = message;
    }

    public ForbiddenException(String message) {
        this.httpStatusCode = 407;
        this.message = message;
        this.defaultMsg = false;
    }
}
