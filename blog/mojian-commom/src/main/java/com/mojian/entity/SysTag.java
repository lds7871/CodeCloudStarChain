package com.mojian.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mojian.utils.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_tag")
@Schema(description = "标签表对象")
public class SysTag implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS)
    private LocalDateTime updateTime;
}
