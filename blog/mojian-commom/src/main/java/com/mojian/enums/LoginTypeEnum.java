package com.mojian.enums;


import lombok.Getter;

/**
 * @author: quequnlong
 * @date: 2024/12/29
 * @description:
 */
@Getter
public enum LoginTypeEnum {

    Account(1,"账号密码登录"),

    EMAIL(2, "邮箱登录"),

    GITEE(3, "码云登录"),

    WECHAT(4, "微信登录");


    private final Integer type;


    private final String desc;

    LoginTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

}
