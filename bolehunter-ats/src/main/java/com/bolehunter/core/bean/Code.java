package com.bolehunter.core.bean;

/**
 * 响应码枚举（与原 lin-cms Code 对齐，保证前端错误码兼容）
 */
public enum Code {

    SUCCESS(0, "成功"),
    CREATED(1, "创建成功"),
    UPDATED(2, "更新成功"),
    DELETED(3, "删除成功"),
    PARAMETER_ERROR(10030, "参数错误"),
    NOT_FOUND(10020, "资源不存在"),
    FAIL(9999, "服务器未知错误"),
    FILE_TOO_LARGE(10110, "文件体积过大"),
    INTERNAL_SERVER_ERROR(9999, "服务器未知错误");

    private final int code;

    private final String description;

    Code(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getZhDescription() {
        return description;
    }
}
