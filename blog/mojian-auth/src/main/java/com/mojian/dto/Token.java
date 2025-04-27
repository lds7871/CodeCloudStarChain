package com.mojian.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: Token
 * @author: Icw
 * @date: 2025/4/20 2:05
 * @Version: 1.0
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    private String access_token;
    private String expires_in;
    private String refresh_token;
    private String openid;
    private String scope;
    private String is_snapchatuser;
    private String unionid;
}
