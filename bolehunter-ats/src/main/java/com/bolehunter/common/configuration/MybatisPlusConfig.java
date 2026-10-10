package com.bolehunter.common.configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.bolehunter.common.tenant.RecruitTenantLineHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置：分页插件始终启用；租户行级隔离由 tenant.enable 控制。
 */
@Configuration(proxyBeanMethods = false)
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            @Value("${tenant.enable:false}") boolean tenantEnable) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 租户拦截器必须放在分页之前
        if (tenantEnable) {
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new RecruitTenantLineHandler()));
        }
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
