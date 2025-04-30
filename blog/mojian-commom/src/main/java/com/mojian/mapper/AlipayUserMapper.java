package com.mojian.mapper;

import com.mojian.dto.user.AlipayOrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @className: AlipayUserMapper
 * @author: Icw
 * @date: 2025/4/29 22:08
 * @Version: 1.0
 * @description:
 */
@Mapper
public interface AlipayUserMapper {
   Integer insertAlipayOrderInfo(@Param("alipayOrderInfo") AlipayOrderInfo alipayOrderInfo);

   Integer payFail(@Param("alipayOrderInfo") AlipayOrderInfo  alipayOrderInfo);
}
