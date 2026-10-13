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

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 接口文档配置（迭代18）
 * <p>访问 /swagger-ui.html；右上角 Authorize 填登录返回的 access_token 即可带鉴权调试。
 * 生产环境通过 API_DOCS_ENABLED=false 关闭文档端点。</p>
 */
@Configuration
public class OpenApiConfiguration {

    private static final String SECURITY_SCHEME = "BearerAuth";

    @Bean
    public OpenAPI bolehunterOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("BoleHunter 伯乐猎 API")
                        .description("开源猎头 ATS + CRM 接口文档（AGPL-3.0-or-later）"
                                + "<br>登录 /recruit/user/login 获取 access_token 后点 Authorize 填入即可调试鉴权接口。")
                        .version("v1.1.0"))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }
}
