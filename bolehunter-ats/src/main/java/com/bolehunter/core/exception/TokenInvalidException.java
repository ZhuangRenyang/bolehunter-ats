package com.bolehunter.core.exception;

public class TokenInvalidException extends HttpException {

    public TokenInvalidException(int code) {
        this.code = code;
        this.httpStatusCode = 401;
    }

    public TokenInvalidException(int code, String message) {
        this.code = code;
        this.httpStatusCode = 401;
        this.message = message;
    }

    public TokenInvalidException(String message) {
        this.httpStatusCode = 401;
        this.message = message;
        this.defaultMsg = false;
    }
}
