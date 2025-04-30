package com.mojian.config.properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云的配置类
 */

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String appPrivateKey;//私钥
    private String alipayPublicKey;//公钥
    private String notifyUrl;//异步通知地址
    //前端用户端充值后返回的地址
    private String returnUrlTwo;
    //前端用户端订单支付返回的地址
    private String returnUrlThree;
}
