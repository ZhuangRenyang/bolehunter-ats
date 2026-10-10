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

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务接口
 */
public interface Uploader {

    /**
     * 上传文件
     *
     * @param fileMap 文件map
     * @return 文件数据
     */
    List<File> upload(MultiValueMap<String, MultipartFile> fileMap);

    /**
     * 上传文件
     *
     * @param fileMap    文件map
     * @param preHandler 预处理器
     * @return 文件数据
     */
    List<File> upload(MultiValueMap<String, MultipartFile> fileMap, PreHandler preHandler);
}
