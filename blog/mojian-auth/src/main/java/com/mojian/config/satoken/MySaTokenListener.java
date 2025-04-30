package com.mojian.config.satoken;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.hutool.json.JSONUtil;
import com.mojian.common.RedisConstants;
import com.mojian.dto.user.GiteeInfo;
import com.mojian.dto.user.WeChatInfo;
import com.mojian.entity.SysUser;
import com.mojian.mapper.GiteeMapper;
import com.mojian.mapper.SysUserMapper;
import com.mojian.mapper.WeChatMapper;
import com.mojian.utils.IpUtil;
import com.mojian.utils.RedisUtil;
import com.mojian.utils.UserAgentUtil;
import com.mojian.vo.user.GiteeOnilneUserVo;
import com.mojian.vo.user.OnlineUserVo;
import com.mojian.vo.user.WxOnlineUserVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 自定义侦听器的实现
 */
@Component
@RequiredArgsConstructor
public class MySaTokenListener implements SaTokenListener {

    private final SysUserMapper userMapper;

    private final HttpServletRequest request;

    private final RedisUtil redisUtil;
    @Autowired
    private WeChatMapper weChatMapper;
    @Autowired
    private GiteeMapper giteeMapper;
    @Value("${sa-token.timeout}")
    private Integer timeout;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    public boolean checkRoleId() {
        Object weChatInfoObj = redisTemplate.opsForValue().get("userInfo");
        if (weChatInfoObj instanceof WeChatInfo) {
            WeChatInfo weChatInfo = (WeChatInfo) weChatInfoObj;
            Integer roleId = weChatInfo.getRoleId();
            return roleId != null && roleId.equals(1);
        }
        return false;
    }


    public boolean checkgiteeRoleId() {
        Object giteeInfoObj = redisTemplate.opsForValue().get("giteeInfo");
        if (giteeInfoObj instanceof GiteeInfo) {
            GiteeInfo giteeInfo = (GiteeInfo) giteeInfoObj;
            Integer roleId = giteeInfo.getRoleId();
            return roleId != null && roleId.equals(1);
        }
        return false;
    }

    /** 每次登录时触发 */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginModel loginModel) {

        String ip = IpUtil.getIp();
        System.out.println("自定义的id"+loginId);
        SysUser user = userMapper.selectById((Integer) loginId);
        WeChatInfo weChatInfo = weChatMapper.selectById((Integer) loginId);
        if(!Objects.isNull(user)) {
            System.out.println(33333);
            // 更新登录信息
            String userAgent = request.getHeader("User-Agent");
            user.setLastLoginTime(LocalDateTime.now());
            user.setIp(ip);
            user.setIpLocation(IpUtil.getIp2region(ip));
            user.setOs(UserAgentUtil.getOs(userAgent));
            user.setBrowser(UserAgentUtil.getBrowser(userAgent));
            userMapper.updateById(user);
            OnlineUserVo onlineUserVo = new OnlineUserVo();
            BeanUtils.copyProperties(user, onlineUserVo);
            onlineUserVo.setTokenValue(tokenValue);
            onlineUserVo.setPassword("");
            redisUtil.set(RedisConstants.LOGIN_TOKEN + tokenValue, JSONUtil.toJsonStr(onlineUserVo), timeout, TimeUnit.SECONDS);
            System.out.println("---------- 自定义侦听器实现 doLogin");
        }else if(!Objects.isNull(weChatInfo)){
                // 更新登录信息
                String userAgent = request.getHeader("User-Agent");
                weChatInfo.setLastLoginTime(String.valueOf(LocalDateTime.now()));
                weChatInfo.setIp(ip);
                weChatInfo.setIpLocation(IpUtil.getIp2region(ip));
                weChatInfo.setOs(UserAgentUtil.getOs(userAgent));
                weChatInfo.setBrowser(UserAgentUtil.getBrowser(userAgent));
                weChatMapper.updateById(weChatInfo);
                WxOnlineUserVo wxOnlineUserVo = new WxOnlineUserVo();
                BeanUtils.copyProperties(weChatInfo, wxOnlineUserVo);

                wxOnlineUserVo.setTokenValue(tokenValue);
                redisUtil.set(RedisConstants.LOGIN_TOKEN + tokenValue, JSONUtil.toJsonStr(wxOnlineUserVo),timeout, TimeUnit.SECONDS);
                System.out.println("---------- 自定义侦听器实现 doLogin");
            }else {
            GiteeInfo gitee = giteeMapper.selectByUserId((Integer) loginId);
            System.out.println(1111111);
            String userAgent = request.getHeader("User-Agent");
            gitee.setLastLoginTime(String.valueOf(LocalDateTime.now()));
            gitee.setIp(ip);
            gitee.setIpLocation(IpUtil.getIp2region(ip));
            gitee.setOs(UserAgentUtil.getOs(userAgent));
            gitee.setBrowser(UserAgentUtil.getBrowser(userAgent));
            giteeMapper.updateByUser(gitee);
            GiteeOnilneUserVo giteeOnilneUserVo = new GiteeOnilneUserVo();
            BeanUtils.copyProperties(gitee, giteeOnilneUserVo);
            giteeOnilneUserVo.setTokenValue(tokenValue);
            System.out.println("原本" + gitee);
            System.out.println("数据为；" + giteeOnilneUserVo.toString());
            redisUtil.set(RedisConstants.LOGIN_TOKEN + tokenValue, JSONUtil.toJsonStr(giteeOnilneUserVo), timeout, TimeUnit.SECONDS);
            System.out.println("---------- 自定义侦听器实现 doLogin");
        }
    }

    /** 每次注销时触发 */
    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        redisUtil.delete(RedisConstants.LOGIN_TOKEN + tokenValue);
        System.out.println("---------- 自定义侦听器实现 doLogout");
    }

    /** 每次被踢下线时触发 */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        redisUtil.delete(RedisConstants.LOGIN_TOKEN + tokenValue);
        System.out.println("---------- 自定义侦听器实现 doKickout");
    }

    /** 每次被顶下线时触发 */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        redisUtil.delete(RedisConstants.LOGIN_TOKEN + tokenValue);
        System.out.println("---------- 自定义侦听器实现 doReplaced");
    }

    /** 每次被封禁时触发 */
    @Override
    public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
        System.out.println("---------- 自定义侦听器实现 doDisable");
    }

    /** 每次被解封时触发 */
    @Override
    public void doUntieDisable(String loginType, Object loginId, String service) {
        System.out.println("---------- 自定义侦听器实现 doUntieDisable");
    }

    /** 每次二级认证时触发 */
    @Override
    public void doOpenSafe(String loginType, String tokenValue, String service, long safeTime) {
        System.out.println("---------- 自定义侦听器实现 doOpenSafe");
    }

    /** 每次退出二级认证时触发 */
    @Override
    public void doCloseSafe(String loginType, String tokenValue, String service) {
        System.out.println("---------- 自定义侦听器实现 doCloseSafe");
    }

    /** 每次创建Session时触发 */
    @Override
    public void doCreateSession(String id) {
        System.out.println("---------- 自定义侦听器实现 doCreateSession");
    }

    /** 每次注销Session时触发 */
    @Override
    public void doLogoutSession(String id) {
        System.out.println("---------- 自定义侦听器实现 doLogoutSession");
    }

    /** 每次Token续期时触发 */
    @Override
    public void doRenewTimeout(String tokenValue, Object loginId, long timeout) {
        System.out.println("---------- 自定义侦听器实现 doRenewTimeout");
    }
}

