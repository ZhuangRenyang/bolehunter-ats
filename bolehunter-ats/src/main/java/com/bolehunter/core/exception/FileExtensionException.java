package com.bolehunter.core.exception;

public class FileExtensionException extends HttpException {

    public FileExtensionException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 400;
        this.message = message;
    }

    public FileExtensionException(String message) {
        this.httpStatusCode = 400;
        this.message = message;
        this.defaultMsg = false;
    }
}
