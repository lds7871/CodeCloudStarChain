package com.mojian.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.common.Constants;
import com.mojian.common.Result;
import com.mojian.entity.FileDetail;
import com.mojian.entity.SysFileOss;
import com.mojian.exception.ServiceException;
import com.mojian.service.FileDetailService;
import com.mojian.utils.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@Tag(name = "文件管理")
@RequiredArgsConstructor
public class FileController {
    private final FileDetailService fileDetailService;
    private final FileStorageService fileStorageService;

    @SaCheckLogin
    @GetMapping("/list")
    @Operation(summary = "获取文件记录表列表")
    public Result<IPage<FileDetail>> list(FileDetail fileDetail) {
        return Result.success(fileDetailService.selectPage(fileDetail));
    }

    @SaCheckLogin
    @GetMapping("/getOssConfig")
    @Operation(summary = "获取存储平台配置")
    public Result<List<SysFileOss>> getOssConfig() {
        return Result.success(fileDetailService.getOssConfig());
    }

    @SaCheckLogin
    @PostMapping("/initLocalStorage")
    @Operation(summary = "初始化本地存储配置")
    public Result<Void> initLocalStorage() {
        // 检查是否已有本地存储配置
        List<SysFileOss> ossConfigs = fileDetailService.getOssConfig();
        boolean hasLocalConfig = ossConfigs.stream()
                .anyMatch(config -> "local".equals(config.getPlatform()));
        
        if (!hasLocalConfig) {
            // 创建本地存储配置
            SysFileOss localConfig = new SysFileOss();
            localConfig.setPlatform("local");
            localConfig.setDomain("http://localhost"); // 默认域名，可以根据需要修改
            localConfig.setBasePath(""); // 基础路径为空
            localConfig.setIsEnable(1); // 启用
            localConfig.setEnableAccess(1); // 启用访问
            localConfig.setPathPatterns("/file/**"); // 访问路径模式
            
            fileDetailService.addOss(localConfig);
            fileStorageService.getProperties().setDefaultPlatform("local");
        } else {
            System.out.println("本地存储配置已存在");
        }
        
        return Result.success();
    }

    @SaCheckLogin
    @PostMapping("/addOss")
    @SaCheckPermission("sys:oss:submit")
    @Operation(summary = "添加存储平台配置")
    public Result<Void> addOss(@RequestBody SysFileOss sysFileOss) {
        fileDetailService.addOss(sysFileOss);
        if (sysFileOss.getIsEnable() == Constants.YES) {
            fileStorageService.getProperties().setDefaultPlatform(sysFileOss.getPlatform());
        }
        return Result.success();
    }

    @SaCheckLogin
    @PutMapping("/updateOss")
    @SaCheckPermission("sys:oss:submit")
    @Operation(summary = "修改存储平台配置")
    public Result<Void> updateOss(@RequestBody SysFileOss sysFileOss) {
        fileDetailService.updateOss(sysFileOss);
        if (sysFileOss.getIsEnable() == Constants.YES) {
            fileStorageService.getProperties().setDefaultPlatform(sysFileOss.getPlatform());
        }
        return Result.success();
    }

    @SaCheckLogin
    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public Result<String> upload(MultipartFile file, String source) {
        try {
            // 对于x-file-storage，setPath应该使用相对路径
            String relativePath;
            String datePath = DateUtil.parseDateToStr(DateUtil.YYYYMMDD, DateUtil.getNowDate()) + "/";
            
            //这个source可在前端上传文件时提供，可用来区分是头像还是文章图片等
            if (StringUtils.isNotBlank(source)) {
                relativePath = datePath + source + "/";
            } else {
                relativePath = datePath + "article/";
            }
            //获取文件名和后缀
            String savedFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            System.out.println("保存的文件名：" + savedFilename);
            // x-file-storage使用相对路径
            FileInfo fileInfo = fileStorageService.of(file)
                    .setPath(relativePath) // 使用相对路径
                    .setSaveFilename(savedFilename)
                    .putAttr("source",source)
                    .upload();
            
            if (fileInfo == null) {
                throw new ServiceException("上传文件失败，fileInfo为null");
            }
            
            System.out.println("文件上传成功，URL：" + fileInfo.getUrl());
            System.out.println("========================");
            return Result.success(fileInfo.getUrl());
        } catch (Exception e) {
            System.err.println("文件上传异常：" + e.getMessage());
            e.printStackTrace();
            throw new ServiceException("文件上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/delete")
    @Operation(summary = "删除文件")
    @SaCheckPermission("sys:file:delete")
    public Result<Boolean> delete(String url) {
        boolean flag = fileStorageService.delete(url);
        if (flag) {
            fileDetailService.delete(url);
        }
        return Result.success();
    }
}
