package com.mojian.dto.user;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @className: WeChatInfo
 * @author: Icw
 * @date: 2025/4/26 18:01
 * @Version: 1.0
 * @description:
 */
@Data

public class WeChatInfo implements Serializable {

    private Integer id;

    private String openid;

    private String nickname;

    private String headimgurl;

    private String sex;

    private String token;

    private List<String> permissions;

    private List<String> roles;

    private Integer roleId;

    private String createTime;

    private String mobile;
    private String email;

}
