package com.mojian.controller;

import com.alibaba.fastjson.JSON;
import com.mojian.common.Result;
import com.mojian.dto.ResponseDTO;
import com.mojian.dto.ThirdlyResult;
import com.mojian.dto.user.GiteeInfo;
import com.mojian.entity.SysUser;
import com.mojian.entity.Users;
import com.mojian.service.OauthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @className: OauthController
 * @author: Icw
 * @date: 2025/4/28 17:46
 * @Version: 1.0
 * @description:
 */
@RestController
@RequestMapping("/oauth")
public class OauthController {
    @Autowired
    private  OauthService oauthService;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @GetMapping("/login")
    @CrossOrigin
    public void thirdlyLogin( HttpServletResponse response) throws IOException {
        String url = oauthService.choiceLoginType();
        response.sendRedirect(url);
    }

    @CrossOrigin
    @GetMapping("/callback")
    public void redirectUri(String code,HttpServletResponse response) throws IOException {
        String result = oauthService.getOauthToken(code);
        ThirdlyResult thirdlyResult = (ThirdlyResult) JSON.parseObject(result, ThirdlyResult.class);
        SysUser userInfo = oauthService.getUserInfo(thirdlyResult.getAccessToken());
        response.sendRedirect("http://127.0.0.1:3000/blogclient/#/?token="+userInfo.getToken());
    }

    @GetMapping("/getGiteeUserInfo")
    @CrossOrigin
    public Result<SysUser> getGiteeUserInfo() {
        SysUser giteeInfo = (SysUser) redisTemplate.opsForValue().get("giteeInfo");
        System.out.println(giteeInfo);
        if(giteeInfo!= null){
            System.out.println(giteeInfo);
            return Result.success(giteeInfo);
        }
        return null;
    }

}
