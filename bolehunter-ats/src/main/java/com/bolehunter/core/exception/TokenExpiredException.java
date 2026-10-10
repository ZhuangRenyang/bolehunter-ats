package com.bolehunter.core.exception;

public class TokenExpiredException extends HttpException {

    public TokenExpiredException(int code) {
        this.code = code;
        this.httpStatusCode = 401;
    }

    public TokenExpiredException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 401;
        this.message = message;
    }

    public TokenExpiredException(String message) {
        this.httpStatusCode = 401;
        this.message = message;
        this.defaultMsg = false;
    }
}
