package com.bolehunter.core.exception;

public class AuthenticationException extends HttpException {

    public AuthenticationException(int code) {
        this.code = code;
        this.httpStatusCode = 401;
    }

    public AuthenticationException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 401;
        this.message = message;
    }

    public AuthenticationException(String message) {
        this.httpStatusCode = 401;
        this.message = message;
        this.defaultMsg = false;
    }
}
