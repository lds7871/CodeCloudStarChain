package com.mojian.controller.monitor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.common.Result;
import com.mojian.service.CacheService;
import com.mojian.vo.cache.CacheInfoVo;
import com.mojian.vo.cache.CacheKeyQuery;
import com.mojian.vo.cache.CacheKeyVo;
import com.mojian.vo.cache.CacheMemoryVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "缓存监控")
@RestController
@RequestMapping("/monitor/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    @Operation(summary = "获取缓存信息")
    @GetMapping("/info")
    public Result<CacheInfoVo> getCacheInfo() {
        return Result.success(cacheService.getCacheInfo());
    }

    @Operation(summary = "获取内存信息")
    @GetMapping("/memory")
    public Result<CacheMemoryVo> getMemoryInfo() {
        return Result.success(cacheService.getMemoryInfo());
    }

    @Operation(summary = "获取缓存键列表")
    @GetMapping("/keys")
    public Result<IPage<CacheKeyVo>> getKeyList(CacheKeyQuery query) {
        return Result.success(cacheService.getKeyList(query));
    }

    @Operation(summary = "清空缓存")
    @DeleteMapping
    public Result<Void> clearCache() {
        cacheService.clearCache();
        return Result.success();
    }
}
