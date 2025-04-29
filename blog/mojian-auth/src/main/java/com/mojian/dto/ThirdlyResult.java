package com.mojian.dto;

import lombok.Data;

/**
 * @className: ThirdlyResult
 * @author: Icw
 * @date: 2025/4/28 17:47
 * @Version: 1.0
 * @description:
 */
@Data
public class ThirdlyResult {
    private String accessToken;

    private String tokenType;

    private Long expiresIn;

    private String refreshToken;

    private String scope;

    private String createAt;
}
