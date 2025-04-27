package com.mojian.service;

import com.mojian.dto.User;
import com.mojian.dto.user.WeChatInfo;

/**
 * @className: WeChatService
 * @author: Icw
 * @date: 2025/4/26 18:03
 * @Version: 1.0
 * @description:
 */

public interface WeChatService {
    WeChatInfo login(WeChatInfo user);
}
