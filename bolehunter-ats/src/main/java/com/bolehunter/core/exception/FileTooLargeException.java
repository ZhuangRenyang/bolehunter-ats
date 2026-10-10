package com.bolehunter.core.exception;

public class FileTooLargeException extends HttpException {

    public FileTooLargeException(int code) {
        this.code = code;
        this.httpStatusCode = 413;
    }

    public FileTooLargeException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 413;
        this.message = message;
    }

    public FileTooLargeException(String message) {
        this.httpStatusCode = 413;
        this.message = message;
        this.defaultMsg = false;
    }
}
