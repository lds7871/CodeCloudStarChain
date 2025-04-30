package com.mojian.dto.user;

import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: AlipayOrderInfo
 * @author: Icw
 * @date: 2025/4/29 21:59
 * @Version: 1.0
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlipayOrderInfo {
    private Integer id;
    private Integer userId;
    private Integer artId;
    private String tradeStatus;
    private String totalAmount;
    private String gmtPayment;
}
