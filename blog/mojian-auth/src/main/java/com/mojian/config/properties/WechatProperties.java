package com.mojian.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {

    private String appId;

    private String appSecret;

    private String domain;

    private String redirectUri;
}
