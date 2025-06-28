package com.mojian.controller.dashboard;

import com.mojian.common.Result;
import com.mojian.service.IndexService;
import com.mojian.vo.dashboard.IndexVo;
import com.mojian.vo.dashboard.VisitTrendVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/sys/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IndexService indexService;

    @GetMapping
    @Operation(summary = "首页")
    public Result<IndexVo> index() {
        return Result.success(indexService.index());
    }


    @GetMapping("/bottom")
    @Operation(summary = "首页底部分类")
    public Result<List<Map<String, Integer>>> getCategories() {
        return Result.success(indexService.getCategories());
    }

    @GetMapping("/trend")
    @Operation(summary = "获取访问趋势数据")
    public Result<VisitTrendVo> getVisitTrend(@RequestParam(value = "type", defaultValue = "week") String type) {
        return Result.success(indexService.getVisitTrend(type));
    }
}
