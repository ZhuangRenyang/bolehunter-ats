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
package com.bolehunter.common.configuration;

import com.bolehunter.common.interceptor.RequestLogInterceptor;
import com.bolehunter.common.tenant.TenantContextInterceptor;
import com.bolehunter.core.interceptor.AuthorizeInterceptor;
import com.bolehunter.core.interceptor.LogInterceptor;
import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Spring MVC 配置
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class WebConfiguration implements WebMvcConfigurer {

    @Value("${auth.enabled:false}")
    private boolean authEnabled;

    @Value("${request-log.enabled:false}")
    private boolean requestLogEnabled;

    @Autowired
    private AuthorizeInterceptor authorizeInterceptor;

    @Autowired
    private LogInterceptor logInterceptor;

    @Autowired
    private RequestLogInterceptor requestLogInterceptor;

    @Autowired
    private TenantContextInterceptor tenantContextInterceptor;

    @Value("${lin.file.store-dir:assets/}")
    private String dir;

    @Value("${lin.file.serve-path:assets/**}")
    private String servePath;

    /**
     * 允许的跨域来源（迭代15 安全加固）
     * <p>逗号分隔，如 https://ats.example.com,https://www.example.com；
     * 默认 * 保持开源开箱即用（dev 本地无妨），生产务必显式配置。</p>
     */
    @Value("${app.cors.allowed-origin-patterns:*}")
    private String corsAllowedOriginPatterns;

    /**
     * 跨域
     * <p>来源由 app.cors.allowed-origin-patterns 配置：开发默认放开，
     * 生产环境应收敛为前端站点显式域名列表（application-prod.yml 已给出示例）。</p>
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Spring 6 不允许 allowedOrigins("*") 与 allowCredentials(true) 组合
                .allowedOriginPatterns(corsAllowedOriginPatterns.split("\\s*,\\s*"))
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (authEnabled) {
            registry.addInterceptor(authorizeInterceptor)
                    .excludePathPatterns(getDirServePath());
        }
        if (requestLogEnabled) {
            registry.addInterceptor(requestLogInterceptor);
        }
        registry.addInterceptor(logInterceptor);
        // 租户上下文：必须注册在鉴权拦截器之后（鉴权后 LocalUser 才可用），负责写入/清理 TenantContext
        registry.addInterceptor(tenantContextInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(getDirServePath())
                .addResourceLocations("file:" + getAbsDir() + "/");
    }

    private String getDirServePath() {
        return servePath;
    }

    /**
     * 获得文件夹的绝对路径
     */
    private String getAbsDir() {
        if (FileUtil.isAbsolutePath(dir)) {
            return dir;
        }
        String cmd = System.getProperty("user.dir");
        Path path = FileSystems.getDefault().getPath(cmd, dir);
        return path.toAbsolutePath().toString();
    }
}
