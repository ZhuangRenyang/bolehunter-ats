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
     * 跨域
     * 注意： 跨域问题涉及安全性问题，这里提供的是最方便简单的配置，任何host和任何方法都可跨域
     * 但在实际场景中，这样做，无疑很危险，所以谨慎选择开启或者关闭
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Spring 6 不允许 allowedOrigins("*") 与 allowCredentials(true) 组合
                .allowedOriginPatterns("*")
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
