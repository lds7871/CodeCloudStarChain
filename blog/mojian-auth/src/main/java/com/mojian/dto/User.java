package com.mojian.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: User
 * @author: Icw
 * @date: 2025/4/20 2:16
 * @Version: 1.0
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String openid;
    private String nickname;
    private String headimgurl;
    private String sex;
    private String city;
    private String province;
    private String country;
    private String unionId;
    private String privilege;


    public String getSex(){
       return this.sex.equals("1")?"女":"男";
    }
}
