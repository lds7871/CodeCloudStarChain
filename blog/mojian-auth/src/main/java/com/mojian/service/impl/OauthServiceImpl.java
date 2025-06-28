package com.mojian.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mojian.common.Constants;
import com.mojian.config.properties.GiteeConfigProperties;
import com.mojian.config.properties.ThirdlyLoginTypeConstant;
import com.mojian.dto.user.GiteeInfo;
import com.mojian.dto.user.WeChatInfo;
import com.mojian.entity.SysUser;
import com.mojian.entity.SysUserRole;
import com.mojian.entity.Users;
import com.mojian.enums.MenuTypeEnum;
import com.mojian.mapper.GiteeMapper;
import com.mojian.mapper.SysMenuMapper;
import com.mojian.mapper.SysRoleMapper;
import com.mojian.mapper.SysUserMapper;
import com.mojian.service.OauthService;
import com.mojian.utils.BeanCopyUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private SysUserMapper sysUserMapper;

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
    public SysUser getUserInfo( String accessToken) {
        String userInfo = "";
        String userInfoUrl = "https://gitee.com/api/v5/user?access_token=" + accessToken;
        userInfo = HttpUtil.createGet(userInfoUrl).execute().body();
        // 将响应字符串解析为JSON对象
        JSONObject jsonObject = JSONUtil.parseObj(userInfo);
        // 从JSON对象中获取id字段的值
        Long id = jsonObject.getLong("id");
//        GiteeInfo giteeInfo = new GiteeInfo();
        Users users = new Users();
        // 使用giteeMapper进行数据库查询
//        giteeInfo = giteeMapper.selectByUniqueId(id);
        users = sysUserMapper.selectByUniqueId(id);
        if(Objects.isNull(users)){
          GiteeInfo giteeInfo = new GiteeInfo();
          Users users1 = new Users();
          giteeInfo  = jsonObject.toBean(GiteeInfo.class);
            System.out.println("giteeInfo:" + giteeInfo);
            users1.setUsername(giteeInfo.getId().toString());
            if(giteeInfo.getEmail().equals("null")){
                users1.setPassword(UUID.randomUUID().toString());
            }else{
                users1.setPassword(giteeInfo.getEmail());
            }
            users1.setAvatar(giteeInfo.getAvatarUrl());
            users1.setLoginType(3);
            users1.setCreateTime(LocalDateTime.now());
            users1.setNickname(giteeInfo.getName());
            users1.setUserInfo(giteeInfo.getSignature());
            users1.setType(2);
            users1.setSex(1);
            users1.setLastLoginTime(LocalDateTime.now());
            Integer insert = sysUserMapper.insert(users1);
            if(insert > 0){
                Users user = sysUserMapper.selectByUniqueId(id);
                StpUtil.login(user.getId());
                String tokenValue = StpUtil.getTokenValue();
//                Users users2 = new Users();
                SysUser sysUser = new SysUser();
                BeanUtils.copyProperties(user,sysUser);
                sysUser.setToken(tokenValue);
                StpUtil.getSession().set(Constants.CURRENT_USER, sysUser);
                redisTemplate.opsForValue().set("giteeInfo",sysUser);
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(sysUser.getId());
                //默认刚注册的用户都为普通用户
                sysUserRole.setRoleId(2);
                System.out.println(sysUserRole);
                roleMapper.insertByUserId(sysUserRole);
//                获取菜单权限列表
                List<String> permissions;
                //刚注册的用户都为普通用户，因此这里直接给普通用户的值
                List<String> roles = roleMapper.selectLists(Constants.UserType);
                System.out.println("角色为："+roles);
//                roles.add(Constants.ADMIN);
                if (roles.contains(Constants.ADMIN)) {
                    permissions = menuMapper.getPermissionList(MenuTypeEnum.BUTTON.getCode());
                    System.out.println("权限为："+permissions);
                } else {
                    permissions = menuMapper.getPermissionListByUserId(giteeInfo.getRoleId(), MenuTypeEnum.BUTTON.getCode());
                }
                sysUser.setRoles(roles);
                sysUser.setPermissions(permissions);
//                StpUtil.login(giteeInfo.getId());
//                String tokenValue = StpUtil.getTokenValue();
//                giteeInfo.setToken(tokenValue);
//                StpUtil.getSession().set(Constants.CURRENT_USER, giteeInfo);
//                redisTemplate.opsForValue().set("giteeInfo",giteeInfo);
//                return giteeInfo;
                return sysUser;
            }else{
                return null;
            }
        }else{

//            Users users2 = new Users();
//            BeanUtils.copyProperties(users,users2);
            SysUser sysUser = new SysUser();
            BeanUtils.copyProperties(users,sysUser);
            StpUtil.login(users.getId());
            String tokenValue = StpUtil.getTokenValue();
            sysUser.setToken(tokenValue);
//        获取菜单权限列表
            List<String> permissions;
            Integer userId = roleMapper.selectRoleId(users.getId());
            //根据用户的id
            List<String> roles = roleMapper.selectLists(userId);
            System.out.println("角色为："+roles);
            if (roles.contains(Constants.ADMIN)) {
                permissions = menuMapper.getPermissionList(MenuTypeEnum.BUTTON.getCode());
                System.out.println("权限为："+permissions);
            } else {
                permissions = menuMapper.getPermissionListByUserId(userId, MenuTypeEnum.BUTTON.getCode());
            }
            sysUser.setRoles(roles);
            sysUser.setPermissions(permissions);
            StpUtil.getSession().set(Constants.CURRENT_USER, sysUser);
            redisTemplate.opsForValue().set("giteeInfo",sysUser);

            return sysUser;
        }
    }
}
