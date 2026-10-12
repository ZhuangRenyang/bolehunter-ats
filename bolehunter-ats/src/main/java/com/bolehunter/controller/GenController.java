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
package com.bolehunter.controller;

import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.service.GenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 代码生成（管理员）—— 表元数据预览与骨架代码 zip 下载
 */
@RestController
@RequestMapping("/recruit/gen")
public class GenController {

    private final GenService genService;

    public GenController(GenService genService) {
        this.genService = genService;
    }

    /**
     * 列出可生成的业务表
     */
    @Logger(template = "查询代码生成表列表")
    @GetMapping("/tables")
    @PermissionMeta(value = "查询代码生成表列表")
    public List<Map<String, Object>> tables() {
        return genService.listTables();
    }

    /**
     * 预览表元数据与字段映射
     */
    @Logger(template = "预览代码生成字段")
    @GetMapping("/preview")
    @PermissionMeta(value = "预览代码生成字段")
    public Map<String, Object> preview(@RequestParam("table_name") String tableName) {
        return genService.preview(tableName);
    }

    /**
     * 生成代码 zip 下载
     */
    @Logger(template = "下载生成代码")
    @PostMapping("/download")
    @PermissionMeta(value = "下载生成代码")
    public void download(@RequestBody Map<String, Object> params, HttpServletResponse response) throws Exception {
        byte[] zip = genService.generate(params);
        String moduleName = String.valueOf(params.getOrDefault("moduleName", "generated"));
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder
                .encode("bolehunter-gen-" + moduleName + ".zip", StandardCharsets.UTF_8));
        response.setContentLength(zip.length);
        response.getOutputStream().write(zip);
    }
}
