package com.mojian.controller.site;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.common.Result;
import com.mojian.entity.SysResource;
import com.mojian.service.SysResourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资源表 控制器
 */
@RestController
@RequestMapping("/sys/resource")
@RequiredArgsConstructor
@Tag(name = "资源表管理")
public class SysResourceController {

    private final SysResourceService sysResourceService;

    @GetMapping("/list")
    @Operation(summary = "获取资源表列表")
    public Result<IPage<SysResource>> list(SysResource sysResource) {
        return Result.success(sysResourceService.selectPage(sysResource));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取资源表详情")
    public Result<SysResource> getInfo(@PathVariable("id") Long id) {
        return Result.success(sysResourceService.getById(id));
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:resource:add")
    @Operation(summary = "添加资源表")
    public Result<Object> add(@RequestBody SysResource sysResource) {
        return Result.success(sysResourceService.insert(sysResource));
    }

    @PutMapping("/update")
    @SaCheckPermission("sys:resource:update")
    @Operation(summary = "修改资源表")
    public Result<Object> edit(@RequestBody SysResource sysResource) {
        return Result.success(sysResourceService.update(sysResource));
    }

    @DeleteMapping("/delete/{ids}")
    @SaCheckPermission("sys:resource:delete")
    @Operation(summary = "删除资源表")
    public Result<Object> remove(@PathVariable List<Long> ids) {
        return Result.success(sysResourceService.deleteByIds(ids));
    }
}
