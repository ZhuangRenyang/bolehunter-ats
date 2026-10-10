package com.bolehunter.core.exception;

/**
 * 业务异常基类：code（业务错误码）+ httpStatusCode（HTTP 状态）+ message。
 * defaultMsg 为 true 时，全局异常处理器优先使用 code-message.properties 中的文案。
 */
public class HttpException extends RuntimeException {

    protected Integer code;

    protected Integer httpStatusCode = 9999;

    protected String message;

    protected boolean defaultMsg = true;

    public HttpException() {
    }

    public HttpException(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getHttpCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public boolean ifDefaultMessage() {
        return defaultMsg;
    }

    public void setDefaultMsg(boolean defaultMsg) {
        this.defaultMsg = defaultMsg;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
