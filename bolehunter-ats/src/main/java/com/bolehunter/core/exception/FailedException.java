package com.bolehunter.core.exception;

public class FailedException extends HttpException {

    public FailedException(int code) {
        this.code = code;
        this.httpStatusCode = 400;
    }

    public FailedException(String message, int code) {
        this.code = code;
        this.httpStatusCode = 400;
        this.message = message;
        this.defaultMsg = false;
    }

    public FailedException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 400;
        this.message = message;
    }

    public FailedException(String message) {
        this.httpStatusCode = 400;
        this.message = message;
        this.defaultMsg = false;
    }
}
