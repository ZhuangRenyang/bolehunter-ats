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
package com.bolehunter.core.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 双令牌（access + refresh），基于 auth0 java-jwt。
 * claim: identity=用户id, type=access|refresh。与原 lin-cms DoubleJWT 对齐。
 */
@Component
public class DoubleJWT {

    private final Algorithm algorithm;

    private final long accessExpireSeconds;

    private final long refreshExpireSeconds;

    public DoubleJWT(@Value("${app.jwt.token-secret}") String secret,
                     @Value("${app.jwt.token-access-expire:3600}") long accessExpireSeconds,
                     @Value("${app.jwt.token-refresh-expire:2592000}") long refreshExpireSeconds) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.accessExpireSeconds = accessExpireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    public Tokens generateTokens(Integer identity) {
        Date now = new Date();
        String accessToken = JWT.create()
                .withClaim("identity", identity)
                .withClaim("type", "access")
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + accessExpireSeconds * 1000))
                .sign(algorithm);
        String refreshToken = JWT.create()
                .withClaim("identity", identity)
                .withClaim("type", "refresh")
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + refreshExpireSeconds * 1000))
                .sign(algorithm);
        return Tokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Map<String, Claim> decodeAccessToken(String token) {
        DecodedJWT jwt = JWT.require(algorithm)
                .withClaim("type", "access")
                .build()
                .verify(token);
        return jwt.getClaims();
    }

    public Map<String, Claim> decodeRefreshToken(String token) {
        DecodedJWT jwt = JWT.require(algorithm)
                .withClaim("type", "refresh")
                .build()
                .verify(token);
        return jwt.getClaims();
    }
}
