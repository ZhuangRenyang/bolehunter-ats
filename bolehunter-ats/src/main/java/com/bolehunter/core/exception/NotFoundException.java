package com.bolehunter.core.exception;

public class NotFoundException extends HttpException {

    public NotFoundException(int code) {
        this.code = code;
        this.httpStatusCode = 404;
    }

    public NotFoundException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 404;
        this.message = message;
    }

    public NotFoundException(String message) {
        this.httpStatusCode = 404;
        this.message = message;
        this.defaultMsg = false;
    }
}
