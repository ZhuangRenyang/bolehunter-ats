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
package com.bolehunter.core.interceptor;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.bolehunter.common.LocalUser;
import com.bolehunter.core.annotation.AdminRequired;
import com.bolehunter.core.annotation.GroupRequired;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import com.bolehunter.core.annotation.RefreshRequired;
import com.bolehunter.core.bean.MetaInfo;
import com.bolehunter.core.exception.AuthenticationException;
import com.bolehunter.core.exception.AuthorizationException;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.core.exception.TokenExpiredException;
import com.bolehunter.core.exception.TokenInvalidException;
import com.bolehunter.core.token.DoubleJWT;
import com.bolehunter.model.PermissionDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.GroupService;
import com.bolehunter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 认证/授权拦截器：合并原 talelin AuthorizeInterceptor + AuthorizeVerifyResolver。
 * 规则与原 lin-cms 一致：
 *   @RefreshRequired → 校验 refresh token；
 *   @LoginRequired   → 校验 access token；
 *   @GroupRequired   → 登录后，root 分组放行，否则校验 module+permission；
 *   @AdminRequired   → 登录后必须为 root 分组；
 *   无注解           → 公开接口。
 */
@Component
public class AuthorizeInterceptor implements HandlerInterceptor {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String BEARER_PATTERN = "^Bearer$";

    @Autowired
    private DoubleJWT jwt;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Value("${lin.file.domain:http://localhost:5000/}")
    private String domain;

    @Value("${lin.file.serve-path:assets/**}")
    private String servePath;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(RefreshRequired.class)) {
            handleRefresh(request);
            return true;
        }
        PermissionMeta permissionMeta = method.getAnnotation(PermissionMeta.class);
        GroupRequired groupRequired = method.getAnnotation(GroupRequired.class);
        AdminRequired adminRequired = method.getAnnotation(AdminRequired.class);
        // @PermissionMeta（无论是否搭配 @GroupRequired）都需要登录 + 权限校验，root 放行
        if (groupRequired != null || permissionMeta != null) {
            handleLogin(request);
            UserDO user = LocalUser.getLocalUser();
            if (verifyAdmin(user)) {
                return true;
            }
            if (permissionMeta != null) {
                verifyPermission(user, buildMeta(method, permissionMeta));
            }
            return true;
        }
        if (adminRequired != null) {
            handleLogin(request);
            UserDO user = LocalUser.getLocalUser();
            if (!verifyAdmin(user)) {
                throw new AuthenticationException(10001);
            }
            return true;
        }
        LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
        if (loginRequired != null) {
            handleLogin(request);
            return true;
        }
        // 无认证注解：公开接口
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束后必须清理 ThreadLocal，防止线程池串数据
        LocalUser.clearLocalUser();
    }

    private void handleLogin(HttpServletRequest request) {
        String tokenStr = verifyHeader(request);
        Map<String, Claim> claims;
        try {
            claims = jwt.decodeAccessToken(tokenStr);
        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            throw new TokenExpiredException(10051);
        } catch (AlgorithmMismatchException | SignatureVerificationException | JWTDecodeException | InvalidClaimException e) {
            throw new TokenInvalidException(10041);
        }
        loadUser(claims);
    }

    private void handleRefresh(HttpServletRequest request) {
        String tokenStr = verifyHeader(request);
        Map<String, Claim> claims;
        try {
            claims = jwt.decodeRefreshToken(tokenStr);
        } catch (com.auth0.jwt.exceptions.TokenExpiredException e) {
            throw new TokenExpiredException(10052);
        } catch (AlgorithmMismatchException | SignatureVerificationException | JWTDecodeException | InvalidClaimException e) {
            throw new TokenInvalidException(10042);
        }
        loadUser(claims);
    }

    private void loadUser(Map<String, Claim> claims) {
        if (claims == null) {
            throw new TokenInvalidException(10041);
        }
        int identity = claims.get("identity").asInt();
        UserDO user = userService.getById(identity);
        if (user == null) {
            throw new NotFoundException(10021);
        }
        // 修正头像为完整 URL
        if (user.getAvatar() != null && !user.getAvatar().startsWith("http")) {
            String prefix = servePath.split("/")[0];
            user.setAvatar(domain + prefix + "/" + user.getAvatar());
        }
        LocalUser.setLocalUser(user);
    }

    private boolean verifyAdmin(UserDO user) {
        return groupService.checkIsRootByUserId(user.getId());
    }

    private void verifyPermission(UserDO user, MetaInfo meta) {
        Integer userId = user.getId();
        List<PermissionDO> permissions = userService.getUserPermissions(userId);
        boolean matched = permissions.stream()
                .anyMatch(it -> it.getModule().equals(meta.getModule())
                        && it.getName().equals(meta.getPermission()));
        if (!matched) {
            throw new AuthenticationException(10001);
        }
    }

    private MetaInfo buildMeta(Method method, PermissionMeta meta) {
        PermissionModule module = method.getAnnotation(PermissionModule.class);
        if (module == null) {
            module = method.getDeclaringClass().getAnnotation(PermissionModule.class);
        }
        String moduleName = module == null ? "" : module.value();
        return MetaInfo.builder().module(moduleName).permission(meta.value()).build();
    }

    private String verifyHeader(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || authorization.trim().isEmpty()) {
            throw new AuthorizationException(10012);
        }
        String[] splits = authorization.split(" ");
        if (splits.length != 2) {
            throw new AuthorizationException(10013);
        }
        String scheme = splits[0];
        String tokenStr = splits[1];
        if (!Pattern.matches(BEARER_PATTERN, scheme)) {
            throw new AuthorizationException(10013);
        }
        return tokenStr;
    }
}
