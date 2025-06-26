package com.mojian.dto.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @className: GiteeInfo
 * @author: Icw
 * @date: 2025/4/20 15:34
 * @Version: 1.0
 * @description: gitee 实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiteeInfo implements Serializable {
    private Integer id;//自增id
    private Integer uniqueId;//第三方平台的唯一id
    private String name;//第三方平台的昵称
    private String avatarUrl;//第三方平台的头像地址
    private String email;//邮箱
    private String mobile;//手机号
    private String lastLoginTime;//上次登录时间
    private String ip;
    private String ipLocation;
    private String os;
    private String browser;
    private String signature;
    private String createdAt;
    private String updatedAt;
    private String token;
    private Integer roleId;
    private List<String> roles;
    private List<String> permissions;
}
