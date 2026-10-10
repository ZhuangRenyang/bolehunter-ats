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
package com.bolehunter.extension.file;

import com.bolehunter.module.file.AbstractUploader;
import com.bolehunter.module.file.FileConstant;
import com.bolehunter.module.file.FileProperties;
import com.bolehunter.module.file.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件上传服务默认实现，上传到本地
 *
 */
@Slf4j
public class LocalUploader extends AbstractUploader {

    @Autowired
    private FileProperties fileProperties;


    @PostConstruct
    public void initStoreDir() {
        // 本地存储需先初始化存储文件夹
        FileUtil.initStoreDir(this.fileProperties.getStoreDir());
    }

    @Override
    protected boolean handleOneFile(byte[] bytes, String newFilename) {
        String absolutePath =
                FileUtil.getFileAbsolutePath(fileProperties.getStoreDir(), getStorePath(newFilename));
        try {
            BufferedOutputStream stream =
                    new BufferedOutputStream(new FileOutputStream(new java.io.File(absolutePath)));
            stream.write(bytes);
            stream.close();
        } catch (Exception e) {
            log.error("写入文件到本地失败:", e);
            // throw new FailedException(10190);
            return false;
        }
        return true;
    }

    @Override
    protected FileProperties getFileProperties() {
        return fileProperties;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected String getStorePath(String newFilename) {
        Date now = new Date();
        String format = new SimpleDateFormat("yyyy/MM/dd").format(now);
        Path path = Paths.get(fileProperties.getStoreDir(), format).toAbsolutePath();
        java.io.File file = new File(path.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        return Paths.get(format, newFilename).toString();
    }

    @Override
    protected String getFileType() {
        return FileConstant.LOCAL;
    }
}
