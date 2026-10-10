package com.bolehunter.common.configuration;

import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.bolehunter.common.interceptor.RequestLogInterceptor;
import com.bolehunter.module.file.FileProperties;
import com.bolehunter.module.log.MDCAccessServletFilter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FileProperties.class)
public class CommonConfiguration {

    @Bean
    public RequestLogInterceptor requestLogInterceptor() {
        return new RequestLogInterceptor();
    }

    @Bean
    public ISqlInjector sqlInjector() {
        return new DefaultSqlInjector();
    }

    /**
     * 接口中，自动转换的有：驼峰转换为下划线，空值输出null
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customJackson() {
        return jacksonObjectMapperBuilder -> {
            // jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
            jacksonObjectMapperBuilder.failOnUnknownProperties(false);
            jacksonObjectMapperBuilder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        };
    }

    /**
     * 用于将 request 相关信息（如请求 url）放入 MDC 中供日志使用
     *
     * @return Logback 的 MDCInsertingServletFilter
     */
    @Bean
    public FilterRegistrationBean<MDCAccessServletFilter> mdcInsertingServletFilter() {
        FilterRegistrationBean<MDCAccessServletFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MDCAccessServletFilter());
        registration.addUrlPatterns("/*");
        registration.setName("mdc-access-servlet-filter");
        return registration;
    }
}
