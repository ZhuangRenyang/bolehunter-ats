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

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.bolehunter.module.file.AbstractUploader;
import com.bolehunter.module.file.FileConstant;
import com.bolehunter.module.file.FileProperties;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;

/**
 * 文件上传服务实现，上传到七牛
 * @author Eval
 */
@Slf4j
public class QiniuUploader  {

    @Autowired
    private FileProperties fileProperties;

    @Value("${lin.file.qiniuyun.access-key}")
    private String accessKey;

    @Value("${lin.file.qiniuyun.secret-key}")
    private String secretKey;

    @Value("${lin.file.qiniuyun.bucket}")
    private String bucket;

    private UploadManager uploadManager;

    private String upToken;

    /**
     * 因为需要得到 spring-boot 提供的配置，所以不能在 constructor 中初始化
     * 使用 PostConstruct 来初始化
     */
    public void initUploadManager() {
        Configuration cfg = new Configuration(Region.region2());
        uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(accessKey, secretKey);
        upToken = auth.uploadToken(bucket);
    }
//
//    @Override
//    protected FileProperties getFileProperties() {
//        return fileProperties;
//    }
//
//    @Override
//    protected String getStorePath(String newFilename) {
//        return fileProperties.getDomain() + newFilename;
//    }
//
//    @Override
//    protected String getFileType() {
//        return FileConstant.REMOTE;
//    }

    /**
     * 处理一个文件数据
     *
     * @param bytes       文件数据，比特流
     * @param newFilename 新文件名称
     * @return 处理是否成功，如果出现异常则返回 false，避免把失败的写入数据库
     */
//    @Override
//    protected boolean handleOneFile(byte[] bytes, String newFilename) {
//        initUploadManager();
//        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
//        try {
//            Response response = uploadManager.put(byteInputStream, newFilename, upToken, null, null);
//            log.info(response.toString());
//            return response.isOK();
//        } catch (QiniuException ex) {
//            Response r = ex.response;
//            log.error("上传文件到七牛云失败: {}", r.error);
//            return false;
//        }
//    }
}
