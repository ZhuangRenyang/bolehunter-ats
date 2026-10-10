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
package com.bolehunter.common.util;

import com.bolehunter.common.observer.Subject;
import com.bolehunter.model.UserDO;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eval
 * @create 2021/4/11 16:39
 */
public class SingleUtil {

    // 这个全局map用于存放被观察者类，key是类名，value是类对象（可以实现单例模式）
    public static Map<String, Subject> map = new HashMap<>();

    // 这个全局map用于存放被观察者每次更新数据时，求职者收到的消息（记得每次使用完都要清空）
    public static Map<String, String> messageMap = new HashMap<>();

    // 保存当前登录用户
    public static UserDO userDO;
}
