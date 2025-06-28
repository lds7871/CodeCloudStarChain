package com.mojian.service;

import com.mojian.dto.user.GiteeInfo;
import com.mojian.entity.SysUser;
import com.mojian.entity.Users;

/**
 * @className: OauthService
 * @author: Icw
 * @date: 2025/4/28 17:43
 * @Version: 1.0
 * @description:
 */

public interface OauthService {
    String choiceLoginType();
    String getOauthToken( String code);
    SysUser getUserInfo(String accessToken);
}
