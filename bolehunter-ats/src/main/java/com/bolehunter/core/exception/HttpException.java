/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
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
