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
package com.bolehunter.service;

import java.util.Map;

/**
 * 代码生成服务 —— 读取表结构元数据，按项目规范渲染后端与前端骨架代码
 */
public interface GenService {

    /**
     * 列出当前数据库中所有业务表（排除系统表与框架表）
     *
     * @return [{tableName, tableComment}]
     */
    java.util.List<Map<String, Object>> listTables();

    /**
     * 预览表列元数据与生成的字段映射
     *
     * @param tableName 表名
     * @return 数据模型（columns 列表与推断结果）
     */
    Map<String, Object> preview(String tableName);

    /**
     * 生成代码并打包为 zip 字节流
     *
     * @param params table_name/module_name/class_name/business_name/author/inherit_base
     * @return zip 字节
     */
    byte[] generate(Map<String, Object> params) throws Exception;

    /**
     * 生成代码预览（不打包，返回 相对路径 -> 文件内容）
     */
    Map<String, String> previewCode(Map<String, Object> params) throws Exception;
}
