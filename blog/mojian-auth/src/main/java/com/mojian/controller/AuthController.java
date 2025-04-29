package com.mojian.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.mojian.common.Result;
import com.mojian.dto.Captcha;
import com.mojian.dto.EmailRegisterDto;
import com.mojian.dto.LoginDTO;
import com.mojian.service.AuthService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mojian.dto.user.*;
import javax.annotation.Resource;
import java.io.IOException;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping("/api/auth/render/{source}")
    public Result<String> renderAuth(HttpServletResponse response, @PathVariable String source) {
        return Result.success(authService.renderAuth(source));
    }

    @RequestMapping("/api/auth/callback/{source}")
    public void login(AuthCallback callback, @PathVariable String source, HttpServletResponse httpServletResponse) throws IOException {
        authService.authLogin(callback,source,httpServletResponse);
    }


    @PostMapping("/auth/login")
    public Result<LoginUserInfo> login(@Validated @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @SaIgnore
    @GetMapping("/auth/getCaptcha")
    public Result<Captcha> getCaptcha() {
        return Result.success(authService.getCaptcha());
    }

    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        redisTemplate.delete("userInfo");
        return Result.success(null);
    }
    @GetMapping("/api/sendEmailCode")
    public Result<Boolean> sendEmailCode(String email) throws MessagingException {
        return Result.success(authService.sendEmailCode(email));
    }

    @PostMapping("/api/email/register")
    public Result<Boolean> register(@RequestBody EmailRegisterDto dto){
        return Result.success(authService.register(dto));
    }

    @PostMapping("/api/email/forgot")
    public Result<Boolean> forgot(@RequestBody EmailRegisterDto dto){
        return Result.success(authService.forgot(dto));
    }

    @GetMapping("/auth/info")
    public Result<LoginUserInfo> getUserInfo(@RequestParam(defaultValue = "admin") String source) {
        return Result.success(authService.getLoginUserInfo(source));
    }

    @GetMapping("/auth/wxinfo")
    public Result<WeChatInfo> getWxUserInfo(@RequestParam String openid,@RequestParam(defaultValue = "admin") String source) {
        return Result.success(authService.getWxLoginUserInfo(openid,source));
    }
}
