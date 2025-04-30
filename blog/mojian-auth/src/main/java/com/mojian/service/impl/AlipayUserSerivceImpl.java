package com.mojian.service.impl;

import com.mojian.dto.user.AlipayOrderInfo;
import com.mojian.mapper.AlipayUserMapper;
import com.mojian.service.AlipayUserSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @className: AlipaySerivceImpl
 * @author: Icw
 * @date: 2025/4/29 22:03
 * @Version: 1.0
 * @description:
 */
@Service
public class AlipayUserSerivceImpl implements AlipayUserSerivce {
    @Autowired
    private AlipayUserMapper alipayMapper;

    @Override
    public Boolean paySuccess(AlipayOrderInfo alipayOrderInfo) {
        System.out.println(alipayOrderInfo);
        Integer res = alipayMapper.insertAlipayOrderInfo(alipayOrderInfo);
        if (res > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean payFail(AlipayOrderInfo alipayOrderInfo) {
        Integer res = alipayMapper.payFail(alipayOrderInfo);
        if (res > 0) {
            return true;
        }
        return false;
    }
}
