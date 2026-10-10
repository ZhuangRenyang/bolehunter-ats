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
package com.bolehunter.common.aop;

import cn.hutool.core.util.StrUtil;
import com.bolehunter.common.configuration.CodeMessageConfiguration;
import com.bolehunter.common.util.SingleUtil;
import com.bolehunter.model.LogDO;
import com.bolehunter.service.LogService;
import com.bolehunter.vo.UnifyResponseVO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 处理返回结果为 UnifyResponseVO 的 Controller
 * message 默认为 null，在此处通过 code 设置为对应消息
 */
@Aspect
@Component
public class ResultAspect {

    @AfterReturning(returning = "result", pointcut = "execution(public * com.bolehunter.controller..*.*(..))")
    public void doAfterReturning(UnifyResponseVO<String> result) {
        int code = result.getCode();
        String oldMessage = result.getMessage();
        // code-message.properties 中配置的 message
        String newMessage = CodeMessageConfiguration.getMessage(code);

        // 如果 code-message.properties 中指定了相应的 message 并且 UnifyResponseVO 的 message 为null
        // 则使用 newMessage 替换 oldMessage
        if (StrUtil.isNotBlank(newMessage) && StrUtil.isBlank(oldMessage)) {
            result.setMessage(newMessage);
        }

    }
}
