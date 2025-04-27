package com.mojian.vo.user;

import com.mojian.dto.user.WeChatInfo;
import lombok.Data;

/**
 * @className: WxOnlineUserVo
 * @author: Icw
 * @date: 2025/4/27 11:40
 * @Version: 1.0
 * @description:
 */
@Data
public class WxOnlineUserVo extends WeChatInfo {
    private String tokenValue;
}
