package com.mojian.controller.message;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.common.Result;
import com.mojian.dto.feedback.SysFeedbackQueryDto;
import com.mojian.entity.SysFeedback;
import com.mojian.service.SysFeedbackService;
import com.mojian.vo.feedback.SysFeedbackVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 反馈表 控制器
 */
@RestController
@RequestMapping("/sys/feedback")
@RequiredArgsConstructor
@Tag(name = "反馈管理")
public class SysFeedbackController {

    private final SysFeedbackService sysFeedbackService;


    @GetMapping("/list")
    @Operation(summary = "获取反馈列表")
    public Result<IPage<SysFeedbackVo>> list(SysFeedbackQueryDto feedbackQueryDto) {
        return Result.success(sysFeedbackService.selectPage(feedbackQueryDto));
    }

    @PostMapping("/add")
    @Operation(summary = "添加反馈")
    @SaCheckPermission("sys:feedback:add")
    public Result<Object> add(@RequestBody SysFeedback sysFeedback) {
        return Result.success(sysFeedbackService.insert(sysFeedback));
    }

    @PutMapping("/update")
    @Operation(summary = "修改反馈")
    @SaCheckPermission("sys:feedback:update")
    public Result<Object> update(@RequestBody SysFeedback sysFeedback) {
        return Result.success(sysFeedbackService.update(sysFeedback));
    }

    @DeleteMapping("/delete/{ids}")
    @Operation(summary = "删除反馈")
    @SaCheckPermission("sys:feedback:delete")
    public Result<Object> delete(@PathVariable List<Long> ids) {
        return Result.success(sysFeedbackService.removeBatchByIds(ids));
    }
}
