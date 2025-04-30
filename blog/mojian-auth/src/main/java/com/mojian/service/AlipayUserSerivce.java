package com.mojian.service;

import com.mojian.dto.user.AlipayOrderInfo;

/**
 * @className: AlipayUserSerivce
 * @author: Icw
 * @date: 2025/4/29 22:07
 * @Version: 1.0
 * @description:
 */

public interface AlipayUserSerivce {
   Boolean paySuccess(AlipayOrderInfo alipayOrderInfo);
   Boolean payFail(AlipayOrderInfo alipayOrderInfo);
}
