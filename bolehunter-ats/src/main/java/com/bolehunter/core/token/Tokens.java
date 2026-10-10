package com.bolehunter.core.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 双令牌（Jackson 全局 SNAKE_CASE 输出为 access_token/refresh_token）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tokens {

    private String accessToken;

    private String refreshToken;
}
