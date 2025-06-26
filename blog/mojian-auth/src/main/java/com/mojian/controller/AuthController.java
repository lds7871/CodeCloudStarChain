package com.mojian.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.mojian.common.Result;
import com.mojian.dto.Captcha;
import com.mojian.dto.EmailRegisterDto;
import com.mojian.dto.LoginDTO;
import com.mojian.dto.user.LoginUserInfo;
import com.mojian.dto.user.WeChatInfo;
import com.mojian.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping("/api/auth/render/{source}")
    public Result<String> renderAuth(HttpServletResponse response, @PathVariable String source) throws AuthException {
        return Result.success(authService.renderAuth(source));
    }

    @RequestMapping("/api/auth/callback/{source}")
    public void login(AuthCallback callback, @PathVariable String source, HttpServletResponse httpServletResponse) throws IOException, AuthException {
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

    @DeleteMapping("/auth/logout")
    public Result<Void> logout(@RequestParam("userInfo") String userInfo ) {
        try {
            StpUtil.logout();

            // 安全删除key
            this.safeDeleteKeys(userInfo);

            return Result.success(null);

        } catch (Exception e) {
            return Result.error("登出失败");
        }
    }
    /**
     * 使用SCAN命令安全删除key（避免阻塞Redis）
     */
    private void safeDeleteKeys(String userInfo) {
        String[] patterns = {"Authorization*", "login:token:*"};

        for (String pattern : patterns) {
            this.scanAndDelete(pattern);
        }
        // 删除单个key
        if(userInfo.equals("gitee")){
            redisTemplate.delete("giteeInfo");
        }else if(userInfo.equals("weixin")){
            redisTemplate.delete("userInfo");
        }
    }

    /**
     * SCAN并删除匹配的key
     */
    private void scanAndDelete(String pattern) {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)  // 每次扫描100个
                    .build();

            Cursor<byte[]> cursor = connection.scan(options);
            int deleteCount = 0;

            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                connection.del(key);
                deleteCount++;

                // 每删除50个key休息一下，避免阻塞
                if (deleteCount % 50 == 0) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {

        } finally {
            connection.close();
        }
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
    public Result<LoginUserInfo> getUserInfo(String source) {
        return Result.success(authService.getLoginUserInfo(source));
    }

    @GetMapping("/auth/wxinfo")
    public Result<WeChatInfo> getWxUserInfo(@RequestParam String openid) {
        return Result.success(authService.getWxLoginUserInfo(openid));
    }

    /**
     * 发送绑定邮箱验证码
     */
    @GetMapping("/api/sendBindEmailCode")
    public Result<Boolean> sendBindEmailCode(String email) throws MessagingException {
        return Result.success(authService.sendBindEmailCode(email));
    }

    /**
     * 绑定邮箱
     */
    @PostMapping("/api/email/bind")
    public Result<Boolean> bindEmail(@RequestBody EmailRegisterDto dto) {
        return Result.success(authService.bindEmail(dto));
    }

    /**
     * 解绑邮箱
     */
    @PostMapping("/api/email/unbind")
    public Result<Boolean> unbindEmail() {
        return Result.success(authService.unbindEmail());
    }
}
