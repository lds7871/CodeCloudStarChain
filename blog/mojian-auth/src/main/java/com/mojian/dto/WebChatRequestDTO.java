package com.mojian.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: ResponseDTO
 * @author: Icw
 * @date: 2025/4/12 23:39
 * @Version: 1.0
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebChatRequestDTO {
    //签名
    private String signature;
    //时间戳
    private String timestamp;
    //随机数
    private String nonce;
    //随机字符串
    private String echostr;
}
