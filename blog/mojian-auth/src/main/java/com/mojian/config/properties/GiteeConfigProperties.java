package com.mojian.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 码云登录配置属性
 *
 * @author quequnlong
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "gitee")
public class GiteeConfigProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
}

