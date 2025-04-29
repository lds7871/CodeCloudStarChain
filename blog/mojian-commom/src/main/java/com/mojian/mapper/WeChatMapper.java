package com.mojian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mojian.dto.user.WeChatInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * @className: WeChatMapper
 * @author: Icw
 * @date: 2025/4/26 18:11
 * @Version: 1.0
 * @description:
 */
@Mapper
public interface WeChatMapper extends BaseMapper<WeChatInfo> {
    WeChatInfo login(@Param("openId") String openId);
    Integer register(@Param("user") WeChatInfo weChatInfo, @Param("now") Date now);
    WeChatInfo selectById(@Param("id") Integer id);
    Integer updateByName(@Param("user") WeChatInfo weChatInfo);
}
