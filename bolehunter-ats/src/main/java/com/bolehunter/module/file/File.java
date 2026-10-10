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
package com.bolehunter.module.file;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {

    /**
     * 真实url
     */
    private String url;

    /**
     * 前端上传的key
     */
    private String key;

    /**
     * 若 local，表示文件路径
     */
    private String path;

    /**
     * LOCAL REMOTE
     */
    private String type;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 扩展名，例：.jpg
     */
    private String extension;

    /**
     * 文件大小
     */
    private Integer size;

    /**
     * md5值，防止上传重复文件
     */
    private String md5;
}
