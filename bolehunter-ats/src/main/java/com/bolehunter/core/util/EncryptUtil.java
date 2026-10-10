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
package com.bolehunter.core.util;

import com.amdelamar.jhash.Hash;
import com.amdelamar.jhash.algorithms.Type;

/**
 * 密码哈希工具：与原 talelin EncryptUtil 完全一致（jhash PBKDF2_SHA256），
 * 保证存量 user_identity 表中的密码哈希继续有效。
 */
public class EncryptUtil {

    private EncryptUtil() {
    }

    public static String encrypt(String credential) {
        return Hash.password(credential.toCharArray())
                .algorithm(Type.PBKDF2_SHA256)
                .create();
    }

    public static boolean verify(String credential, String password) {
        try {
            return Hash.password(password.toCharArray())
                    .algorithm(Type.PBKDF2_SHA256)
                    .verify(credential);
        } catch (Exception e) {
            // 存量哈希格式非法时视为校验失败
            return false;
        }
    }
}
