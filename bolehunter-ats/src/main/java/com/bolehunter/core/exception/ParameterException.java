package com.bolehunter.core.exception;

public class ParameterException extends HttpException {

    public ParameterException(int code) {
        this.code = code;
        this.httpStatusCode = 400;
    }

    public ParameterException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 400;
        this.message = message;
    }

    public ParameterException(String message) {
        this.httpStatusCode = 400;
        this.message = message;
        this.defaultMsg = false;
    }
}
