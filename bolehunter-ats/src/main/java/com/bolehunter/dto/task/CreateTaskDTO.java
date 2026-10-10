/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.dto.task;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Date;

/**
 * 新增任务
 */
@Data
@NoArgsConstructor
public class CreateTaskDTO {

    @NotBlank(message = "{task.title.not-blank}")
    @Size(max = 100, message = "{task.title.not-blank}")
    private String title;

    @Size(max = 500, message = "{task.content.length}")
    private String content;

    /**
     * 到期时间
     */
    @NotNull(message = "{task.due-time.not-null}")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dueTime;

    /**
     * 关联对象类型（可选）
     */
    @Pattern(regexp = "recommendation|client|candidate", message = "{candidate.source.illegal}")
    private String relatedType;

    private Integer relatedId;
}
