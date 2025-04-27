package com.mojian.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;

import com.alibaba.fastjson.JSON;
import com.mojian.config.properties.WechatProperties;
import com.mojian.dto.ResponseDTO;
import com.mojian.dto.Token;
import com.mojian.dto.User;
import com.mojian.dto.WebChatRequestDTO;
import com.mojian.dto.user.WeChatInfo;
import com.mojian.service.WeChatService;
import com.mojian.utils.BeanCopyUtil;
import com.mojian.utils.HttpRequestUtils;
import com.mojian.utils.WebChatUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.net.URLEncoder;

/**
 * @className: WeChatController
 * @author: Icw
 * @date: 2025/4/19 23:30
 * @Version: 1.0
 * @description: 微信登录相关接口
 */
@RestController
@PermitAll
@RequestMapping("/wechat")
public class WeChatController {
    @Autowired
    private WechatProperties wechatProperties;

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @GetMapping("/checkQrCodeStatus")
    public ResponseDTO checkQrCodeStatus() {
        WeChatInfo weChatInfo = (WeChatInfo) redisTemplate.opsForValue().get("userInfo");
        if(weChatInfo!= null){
            return ResponseDTO.success(weChatInfo);
        }
        return null;
    }

    @GetMapping
    public String checkWebChat(WebChatRequestDTO requestDTO) throws NoSuchAlgorithmException {
        if (WebChatUtils.checkSignature(requestDTO)) {
            return requestDTO.getEchostr();//测试通过
        }
        return "error";//测试失败
    }

    @GetMapping("/qrCode")
    @CrossOrigin
    public String getQrCode() throws Exception {
        String qrCode = "";
        System.out.println(1111111);
        // 确保redirect_uri是编码过的，并且指向你的授权处理方法
        String encodedRedirectUri = URLEncoder.encode(wechatProperties.getDomain() + "/wechat/authCallback", "UTF-8");
        String content = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
        content = content.replace("APPID", wechatProperties.getAppId())
                .replace("REDIRECT_URI", encodedRedirectUri);
        int width = 200;
        int height = 200;
        QrConfig config = new QrConfig(width, height);
        qrCode = QrCodeUtil.generateAsBase64(content, config, "png");
        System.out.println(qrCode);
        return qrCode;
    }

    @GetMapping("/authCallback")
    @CrossOrigin
    public ResponseEntity<WeChatInfo> authCallback(String code, String state) throws IOException {
        System.out.println("进来了" );
        if (code == null || state == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
        url = url.replace("APPID", wechatProperties.getAppId())
                .replace("SECRET", wechatProperties.getAppSecret())
                .replace("CODE", code);

        HttpResponse httpResponse = HttpRequestUtils.doGet(url);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            HttpEntity httpEntity = httpResponse.getEntity();
            String string = EntityUtils.toString(httpEntity);
            Token token = JSON.parseObject(string, Token.class);
            String accessToken = token.getAccess_token();
            String openId = token.getOpenid();

            String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
            userInfoUrl = userInfoUrl.replace("ACCESS_TOKEN", accessToken)
                    .replace("OPENID", openId);
            HttpResponse userInfoHttpResponse = HttpRequestUtils.doGet(userInfoUrl);
            int userInfoStatusCode = userInfoHttpResponse.getStatusLine().getStatusCode();
            if (userInfoStatusCode == 200) {
                HttpEntity userInfoHttpEntity = userInfoHttpResponse.getEntity();
                String userInfoString = EntityUtils.toString(userInfoHttpEntity, "UTF-8");
                User user = JSON.parseObject(userInfoString, User.class);
                WeChatInfo weChatInfo = BeanCopyUtil.copyObj(user, WeChatInfo.class);
                System.out.println("微信"+weChatInfo);
                WeChatInfo loggedInInfo = weChatService.login(weChatInfo);
                if (loggedInInfo != null) {
                    return new ResponseEntity<>(loggedInInfo, HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

}
