package com.mojian.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.mojian.common.Constants;
import com.mojian.dto.user.WeChatInfo;
import com.mojian.entity.SysUserRole;
import com.mojian.entity.Users;
import com.mojian.enums.MenuTypeEnum;
import com.mojian.mapper.SysMenuMapper;
import com.mojian.mapper.SysRoleMapper;
import com.mojian.mapper.SysUserMapper;
import com.mojian.mapper.WeChatMapper;
import com.mojian.service.WeChatService;
import com.mojian.utils.BeanCopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @className: WeChatServiceImpl
 * @author: Icw
 * @date: 2025/4/26 18:04
 * @Version: 1.0
 * @description:
 */
@Service
public class WeChatServiceImpl implements WeChatService {
    @Autowired
    private WeChatMapper weChatMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    /**
     * 更新二维码状态
     */

    @Override
    public WeChatInfo login(WeChatInfo user) {
        //先进行一次登录，判断该用户是否已经注册过
        Users login = userMapper.login(user.getOpenid());
        System.out.println("登录信息：" + login);
        //如果没有注册过，执行注册
        if (login == null) {
            System.out.println("头像为：" + user.getHeadimgurl());
            Date now = new Date();
            int userSex = 1;
            if (!Objects.equals(user.getSex(), "男")) {
                userSex = 2;
            }
            Users sysUser = Users.builder()
                    .username(user.getOpenid())
                    .password(BCrypt.hashpw(user.getOpenid()))
                    .nickname(user.getNickname())
                    .avatar(user.getHeadimgurl())
                    .loginType(2)
                    .sex(userSex)
                    .createTime(LocalDateTime.now())
                    .type(2)
                    .status(Constants.YES)
                    .build();
            Long register = (long) userMapper.insert(sysUser);
//            Integer register = weChatMapper.register(user,now);
            if (register > 0) {
                Users users = userMapper.login(user.getOpenid());
                // 执行登录
                StpUtil.login(users.getId());
                String tokenValue = StpUtil.getTokenValue();
                WeChatInfo weChatInfo = BeanCopyUtil.copyObj(users, WeChatInfo.class);
                weChatInfo.setToken(tokenValue);
                weChatInfo.setOpenid(users.getUsername());
                weChatInfo.setSex(users.getSex());
                weChatInfo.setType(users.getType());
                weChatInfo.setEmail(users.getEmail());
                weChatInfo.setMobile(users.getMobile());
                weChatInfo.setUserInfo(users.getUserInfo());
                weChatInfo.setHeadimgurl(users.getAvatar());
                weChatInfo.setNickname(users.getNickname());
                weChatInfo.setId(users.getId());
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(users.getId());
                //默认刚注册的用户都为普通用户
                sysUserRole.setRoleId(2);
                System.out.println(sysUserRole);
                roleMapper.insertByUserId(sysUserRole);

                List<String> permissions;
                //刚注册的用户都为普通用户，因此这里直接给普通用户的值
                List<String> roles = roleMapper.selectLists(Constants.UserType.longValue());
                System.out.println("角色为：" + roles);
                if (roles.contains(Constants.ADMIN) || roles.contains(Constants.SUPERADMIN)) {
                    permissions = menuMapper.getPermissionList(MenuTypeEnum.BUTTON.getCode());
                    System.out.println("权限为：" + permissions);
                } else {
                    permissions = menuMapper.getPermissionListByUserId(users.getId(), MenuTypeEnum.BUTTON.getCode());
                }
                weChatInfo.setRoles(roles);
                weChatInfo.setPermissions(permissions);

                StpUtil.getSession().set(Constants.CURRENT_USER, weChatInfo);
                System.out.println(weChatInfo);
                redisTemplate.opsForValue().set("userInfo", weChatInfo);
                return weChatInfo;
            }
        }
        if (login != null) {
            StpUtil.login(login.getId());
            String tokenValue = StpUtil.getTokenValue();
            WeChatInfo weChatInfo = BeanCopyUtil.copyObj(login, WeChatInfo.class);
            if (weChatInfo != null) {
                weChatInfo.setToken(tokenValue);
                weChatInfo.setOpenid(login.getUsername());
                weChatInfo.setSex(login.getSex());
                weChatInfo.setType(login.getType());
                weChatInfo.setEmail(login.getEmail());
                weChatInfo.setMobile(login.getMobile());
                weChatInfo.setUserInfo(login.getUserInfo());
                weChatInfo.setHeadimgurl(login.getAvatar());
                weChatInfo.setId(login.getId());
                List<String> permissions;
                Long res = roleMapper.selectRoleId(login.getId());
                List<String> roles = roleMapper.selectLists(res);
                System.out.println("角色为：" + roles);
                if (roles.contains(Constants.ADMIN) || roles.contains(Constants.SUPERADMIN)) {
                    permissions = menuMapper.getPermissionList(MenuTypeEnum.BUTTON.getCode());
                    System.out.println("权限为：" + permissions);
                } else {
                    permissions = menuMapper.getPermissionListByUserId(login.getId(), MenuTypeEnum.BUTTON.getCode());
                }
                weChatInfo.setRoles(roles);
                weChatInfo.setPermissions(permissions);
                StpUtil.getSession().set(Constants.CURRENT_USER, weChatInfo);
                redisTemplate.opsForValue().set("userInfo", weChatInfo);
                System.out.println(weChatInfo);
                return weChatInfo;
            }
        }
        throw new RuntimeException();
    }
}
