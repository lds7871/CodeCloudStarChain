package com.mojian.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @className: SysUserRole
 * @author: Icw
 * @date: 2025/6/25 18:58
 * @Version: 1.0
 * @description:
 */
@Data
@TableName("chat_msg")
@Schema(description = "角色-对象关联")
public class SysUserRole {
    private Integer id;
    private Integer roleId;
    private Integer userId;
}
