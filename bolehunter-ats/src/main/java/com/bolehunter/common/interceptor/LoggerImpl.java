package com.bolehunter.common.interceptor;

import com.bolehunter.common.LocalUser;
import com.bolehunter.core.bean.MetaInfo;
import com.bolehunter.core.util.BeanUtil;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 行为日志落库（由 core LogInterceptor 调用）
 */
@Slf4j
@Component
public class LoggerImpl {

    @Autowired
    private LogService logService;

    /**
     * 日志格式匹配正则
     */
    private static final Pattern LOG_PATTERN = Pattern.compile("(?<=\\{)[^}]*(?=})");

    public void handle(com.bolehunter.core.annotation.PermissionMeta meta, MetaInfo metaInfo,
                       com.bolehunter.core.annotation.Logger logger,
                       HttpServletRequest request, HttpServletResponse response) {
        String template = logger.template();
        UserDO user = LocalUser.getLocalUser();
        template = this.parseTemplate(template, user, request, response);
        String permission = "";
        if (metaInfo != null) {
            permission = metaInfo.getPermission();
        }
        Integer userId = user == null ? null : user.getId();
        String username = user == null ? "" : user.getUsername();
        String method = request.getMethod();
        String path = request.getServletPath();
        Integer status = response.getStatus();
        logService.createLog(template, permission, userId, username, method, path, status);
    }

    private String parseTemplate(String template, UserDO user, HttpServletRequest request, HttpServletResponse response) {
        Matcher m = LOG_PATTERN.matcher(template);
        while (m.find()) {
            String group = m.group();
            String property = this.extractProperty(group, user, request, response);
            template = template.replace("{" + group + "}", property);
        }
        return template;
    }

    private String extractProperty(String item, UserDO user, HttpServletRequest request, HttpServletResponse response) {
        int i = item.lastIndexOf('.');
        String obj = item.substring(0, i);
        String prop = item.substring(i + 1);
        switch (obj) {
            case "user":
                if (user == null) {
                    return "";
                }
                return BeanUtil.getValueByPropName(user, prop);
            case "request":
                return BeanUtil.getValueByPropName(request, prop);
            case "response":
                return BeanUtil.getValueByPropName(response, prop);
            default:
                return "";
        }
    }
}
