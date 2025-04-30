package com.mojian.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: AlipayVO
 * @author: Icw
 * @date: 2025/3/7 11:04
 * @Version: 1.0
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlipayVO {
    private String traceNo;//单号
    private double totalAmount;//总价
    private String subject;//商品名称
    private String alipayTraceNo;
//http://localhost:9090/alipay/pay?name=购买榴莲订单&orderNo=8&total=99.00
}
