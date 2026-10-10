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
package com.bolehunter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.model.LogDO;

import java.util.Date;

public interface LogService extends IService<LogDO> {

    /**
     * 分页获取日志
     *
     * @param page  当前页
     * @param count 当前页数目
     * @param name  用户名
     * @param start 日志开启时间
     * @param end   日志结束时间
     * @return 日志数据
     */
    IPage<LogDO> getLogPage(Integer page, Integer count, String name, Date start, Date end);

    /**
     * 分页搜索日志
     *
     * @param page    当前页
     * @param count   当前页数目
     * @param name    用户名
     * @param keyword 搜索关键字
     * @param start   日志开启时间
     * @param end     日志结束时间
     * @return 日志数据
     */
    IPage<LogDO> searchLogPage(Integer page, Integer count, String name, String keyword, Date start, Date end);

    /**
     * 分页获取日志用户名（以被记录日志的用户）
     *
     * @param page  当前页
     * @param count 当前页数目
     * @return 用户名
     */
    IPage<String> getUserNamePage(Integer page, Integer count);

    /**
     * 创建一条日志记录
     *
     * @param message    日志消息
     * @param permission 日志涉及的权限
     * @param userId     用户 id
     * @param username   用户名
     * @param method     请求（http）方法
     * @param path       请求路径
     * @param status     相应状态（http status）
     * @return 是否成功
     */
    boolean createLog(String message, String permission, Integer userId,
                      String username, String method, String path,
                      Integer status);
}
