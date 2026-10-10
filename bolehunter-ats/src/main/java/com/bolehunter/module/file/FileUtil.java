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

import org.springframework.util.DigestUtils;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class FileUtil {

    public static FileSystem getDefaultFileSystem() {
        return FileSystems.getDefault();
    }

    public static boolean isAbsolute(String str) {
        Path path = getDefaultFileSystem().getPath(str);
        return path.isAbsolute();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void initStoreDir(String dir) {
        String absDir;
        if (isAbsolute(dir)) {
            absDir = dir;
        } else {
            String cmd = getCmd();
            Path path = getDefaultFileSystem().getPath(cmd, dir);
            absDir = path.toAbsolutePath().toString();
        }
        File file = new File(absDir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String getCmd() {
        return System.getProperty("user.dir");
    }

    public static String getFileAbsolutePath(String dir, String filename) {
        if (isAbsolute(dir)) {
            return getDefaultFileSystem()
                    .getPath(dir, filename)
                    .toAbsolutePath().toString();
        } else {
            return getDefaultFileSystem()
                    .getPath(getCmd(), dir, filename)
                    .toAbsolutePath().toString();
        }
    }

    public static String getFileExt(String filename) {
        int index = filename.lastIndexOf('.');
        return filename.substring(index);
    }

    public static String getFileMD5(byte[] bytes) {
        return DigestUtils.md5DigestAsHex(bytes);
    }

    public static Long parseSize(String size) {
        DataSize singleLimitData = DataSize.parse(size);
        return singleLimitData.toBytes();
    }
}
