package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mojian.common.Constants;
import com.mojian.config.properties.GiteeConfigProperties;
import com.mojian.config.properties.ThirdlyLoginTypeConstant;
import com.mojian.dto.user.GiteeInfo;
import com.mojian.dto.user.WeChatInfo;
import com.mojian.enums.MenuTypeEnum;
import com.mojian.mapper.GiteeMapper;
import com.mojian.mapper.SysMenuMapper;
import com.mojian.mapper.SysRoleMapper;
import com.mojian.service.OauthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @className: OauthServiceImpl
 * @author: Icw
 * @date: 2025/4/28 17:44
 * @Version: 1.0
 * @description:
 */
@Service
public class OauthServiceImpl implements OauthService {
    @Autowired
    private GiteeConfigProperties giteeProperties;
    @Autowired
    private GiteeMapper giteeMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysMenuMapper menuMapper;
    @Override
    public String choiceLoginType() {
        String url = "";
            url = ThirdlyLoginTypeConstant.GITEE_URL
                    .replace("{client_id}", giteeProperties.getClientId()).replace("{redirect_uri}", giteeProperties.getRedirectUri());
                return url;
    }

    @Override
    public String getOauthToken( String code) {
        Map<String, Object> map = new HashMap<>();
        String result = "";
            String url = ThirdlyLoginTypeConstant.GITEE_OAUTH_TOKEN_URL;
            map.put("grant_type", "authorization_code");
            map.put("code", code);
            map.put("client_id", giteeProperties.getClientId());
            map.put("client_secret", giteeProperties.getClientSecret());
            map.put("redirect_uri", giteeProperties.getRedirectUri());
            //发送get请求并接收响应数据
            result = HttpUtil.createPost(url).form(map).execute().body();
        return result;
    }

    @Override
    public GiteeInfo getUserInfo( String accessToken) {
        String userInfo = "";
        String userInfoUrl = "https://gitee.com/api/v5/user?access_token=" + accessToken;
        userInfo = HttpUtil.createGet(userInfoUrl).execute().body();
        // 将响应字符串解析为JSON对象
        JSONObject jsonObject = JSONUtil.parseObj(userInfo);
        // 从JSON对象中获取id字段的值
        Long id = jsonObject.getLong("id");
        GiteeInfo giteeInfo = new GiteeInfo();
        // 使用giteeMapper进行数据库查询
        giteeInfo = giteeMapper.selectByUniqueId(id);
        if(Objects.isNull(giteeInfo)){
          giteeInfo  = jsonObject.toBean(GiteeInfo.class);
            System.out.println("giteeInfo:" + giteeInfo);
            Integer insert = giteeMapper.insertUserInfo(giteeInfo);
            if(insert > 0){
               GiteeInfo  user = giteeMapper.selectByUniqueId(id);
                StpUtil.login(user.getId());
                String tokenValue = StpUtil.getTokenValue();
                user.setToken(tokenValue);
                StpUtil.getSession().set(Constants.CURRENT_USER, user);
                redisTemplate.opsForValue().set("giteeInfo",user);
                return user;
            }else{
                return null;
            }
        }
        //获取菜单权限列表

            List<String> permissions;
            List<String> roles = roleMapper.selectLists(giteeInfo.getRoleId());
            System.out.println("角色为："+roles);
            roles.add(Constants.ADMIN);
            if (roles.contains(Constants.ADMIN)) {
                permissions = menuMapper.getPermissionList(MenuTypeEnum.BUTTON.getCode());
                System.out.println("权限为："+permissions);
            } else {
                permissions = menuMapper.getPermissionListByUserId(giteeInfo.getRoleId(), MenuTypeEnum.BUTTON.getCode());
            }
        giteeInfo.setRoles(roles);
        giteeInfo.setPermissions(permissions);
        StpUtil.login(giteeInfo.getId());
        String tokenValue = StpUtil.getTokenValue();
        giteeInfo.setToken(tokenValue);
        StpUtil.getSession().set(Constants.CURRENT_USER, giteeInfo);
        redisTemplate.opsForValue().set("giteeInfo",giteeInfo);
        return giteeInfo;
    }
}
