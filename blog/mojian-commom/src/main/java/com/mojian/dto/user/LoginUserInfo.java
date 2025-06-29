package com.mojian.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "登录用户信息")
public class LoginUserInfo implements Serializable {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别")
    private Integer sex;

    @Schema(description = "个人简介")
    private String userInfo;

    @Schema(description = "用户类型(1:管理员，2：普通用户，3：超级管理员)")
    private Integer type;

    @Schema(description = "token")
    private String token;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "角色信息")
    private List<String> roles;
}
