package com.mojian.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitTrendVo {

    @Schema(description = "日期标签")
    private List<String> dateLabels;

    @Schema(description = "访问量数据")
    private List<Integer> visitData;

    @Schema(description = "浏览量数据")
    private List<Integer> viewData;
} 