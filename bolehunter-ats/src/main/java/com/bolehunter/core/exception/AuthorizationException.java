package com.bolehunter.core.exception;

public class AuthorizationException extends HttpException {

    public AuthorizationException(int code) {
        this.code = code;
        this.httpStatusCode = 401;
    }

    public AuthorizationException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 401;
        this.message = message;
    }

    public AuthorizationException(String message) {
        this.httpStatusCode = 401;
        this.message = message;
        this.defaultMsg = false;
    }
}
