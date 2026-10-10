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
