package com.mojian.vo.user;

import com.mojian.dto.user.WeChatInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @className: WxUserInfo
 * @author: Icw
 * @date: 2025/4/27 9:46
 * @Version: 1.0
 * @description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class WxUserInfo {

    private WeChatInfo weChatInfo;

    private List<String> roles;

}
