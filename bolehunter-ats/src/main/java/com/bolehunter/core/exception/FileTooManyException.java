package com.bolehunter.core.exception;

public class FileTooManyException extends HttpException {

    public FileTooManyException(int code) {
        this.code = code;
        this.httpStatusCode = 413;
    }

    public FileTooManyException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 413;
        this.message = message;
    }

    public FileTooManyException(String message) {
        this.httpStatusCode = 413;
        this.message = message;
        this.defaultMsg = false;
    }
}
