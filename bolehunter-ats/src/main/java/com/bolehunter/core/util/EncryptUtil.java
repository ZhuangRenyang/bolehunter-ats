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
