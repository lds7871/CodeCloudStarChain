package com.mojian.utils;

import com.mojian.dto.WebChatRequestDTO;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @className: WebChatUtils
 * @author: Icw
 * @date: 2025/4/19 23:39
 * @Version: 1.0
 * @description:
 */

public class WebChatUtils {
    /**
     * 微信公众平台-测试公众号-接口配置信息-Token
     */
    private static String webChatToken = "wxtoken";
    /**
     * 核实微信的请求
     */
    public static boolean checkSignature(WebChatRequestDTO requestDTO) throws NoSuchAlgorithmException {
        String [] arr = {webChatToken, requestDTO.getTimestamp(), requestDTO.getNonce()};
        // 对数组进行排序
        Arrays.sort(arr);
        //将三个参数字符串拼接成一个字符串进行sha1加密
        String str = String.join("", arr);
        //进行sha1加密
         MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
         byte[] digest = messageDigest.digest(str.getBytes());
         StringBuilder hexString = new StringBuilder();
         for (byte b : digest) {
             String hex = Integer.toHexString(0xff & b);
             if (hex.length() == 1) {
                 hexString.append('0');
             }
             hexString.append(hex);
         }
         return requestDTO.getSignature().equals(hexString.toString());
    }

}
