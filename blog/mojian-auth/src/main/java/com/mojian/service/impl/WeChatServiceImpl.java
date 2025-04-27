package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mojian.common.Constants;
import com.mojian.dto.user.WeChatInfo;
import com.mojian.mapper.WeChatMapper;
import com.mojian.service.WeChatService;
import com.mojian.utils.BeanCopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;

/**
 * @className: WeChatServiceImpl
 * @author: Icw
 * @date: 2025/4/26 18:04
 * @Version: 1.0
 * @description:
 */
@Service
public class WeChatServiceImpl implements WeChatService {
    @Autowired
    private WeChatMapper weChatMapper;

    @Autowired
    private  RedisTemplate<String, Object> redisTemplate;

    /**
     * 更新二维码状态
     */

    @Override
    public WeChatInfo login(WeChatInfo user) {
        WeChatInfo login = weChatMapper.login(user.getOpenid());
        System.out.println("登录信息："+login);
        if (login == null) {
            System.out.println("头像为："+user.getHeadimgurl());
            Date now = new Date();
            Integer register = weChatMapper.register(user,now);
            if (register > 0) {
                WeChatInfo relogin = weChatMapper.login(user.getOpenid());
                // 执行登录
                StpUtil.login(relogin.getId());
                String tokenValue = StpUtil.getTokenValue();
                WeChatInfo weChatInfo = BeanCopyUtil.copyObj(relogin, WeChatInfo.class);
                weChatInfo.setToken(tokenValue);
                System.out.println("存进去的是："+weChatInfo);
                StpUtil.getSession().set(Constants.CURRENT_USER, weChatInfo);

                System.out.println(weChatInfo);
                redisTemplate.opsForValue().set("userInfo",weChatInfo);
                return weChatInfo;
            }
        }
        if (login != null) {
            StpUtil.login(login.getId());
        }
        String tokenValue = StpUtil.getTokenValue();

        WeChatInfo weChatInfo = BeanCopyUtil.copyObj(login, WeChatInfo.class);
        System.out.println("数据为："+weChatInfo);
        if (weChatInfo != null) {
            weChatInfo.setToken(tokenValue);
        }
        System.out.println("存进去的是："+weChatInfo);
        StpUtil.getSession().set(Constants.CURRENT_USER, weChatInfo);
        if (weChatInfo != null) {
            redisTemplate.opsForValue().set("userInfo",weChatInfo);
        }else{
            System.out.println(1111111);
        }
        return weChatInfo;
    }
}
