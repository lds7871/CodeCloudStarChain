package com.mojian.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.common.Result;
import com.mojian.dto.user.WeChatInfo;
import com.mojian.entity.SysArticle;
import com.mojian.entity.SysUser;
import com.mojian.entity.Users;
import com.mojian.service.UserService;
import com.mojian.vo.article.ArticleListVo;
import com.mojian.vo.comment.CommentListVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: quequnlong
 * @date: 2025/1/11
 * @description:
 */
@RestController
@RequestMapping("/protal/user")
@RequiredArgsConstructor
@Tag(name = "门户-个人中心")
public class UserController {

    private final UserService userService;

    @PutMapping("/updateProfile")
    @Operation(summary = "修改我的资料")
    public Result<Void> updateProfile(@RequestBody Users user){
        userService.updateProfile(user);
        return Result.success();
    }
    @PutMapping("/updatewxProfile")
    @Operation(summary = "修改我的资料")
    public Result<Void> updateProfile(@RequestBody WeChatInfo user){
        userService.updatewxProfile(user);
        return Result.success();
    }
    @GetMapping("/comment")
    @Operation(summary = "获取我的评论")
    public Result<IPage<CommentListVo>> selectMyComment(){
        return Result.success(userService.selectMyComment());
    }

    @DeleteMapping("/delMyComment/{ids}")
    @Operation(summary = "删除我的评论")
    public Result<Void> delMyComment(@PathVariable List<Long> ids){
        return Result.success(userService.delMyComment(ids));
    }

    @GetMapping("/myReply")
    @Operation(description = "获取我的回复")
    public Result<IPage<CommentListVo>> getMyReply() {
        return Result.success(userService.getMyReply());
    }

    @GetMapping("/myLike")
    @Operation(summary = "获取我的点赞")
    public Result<IPage<ArticleListVo>> selectMyLike(){
        return Result.success(userService.selectMyLike());
    }

    @GetMapping("/myArticle")
    @Operation(summary = "获取我的文章")
    public Result<IPage<ArticleListVo>> selectMyArticle(SysArticle article){
        return Result.success(userService.selectMyArticle(article));
    }
    @GetMapping("/selectMyBalance")
    @Operation(summary = "获取我的余额")
    public Result<Double>  selectMyBalance(){
        return  Result.success(userService.selectMyBalance(StpUtil.getLoginIdAsLong()));
    }
}
