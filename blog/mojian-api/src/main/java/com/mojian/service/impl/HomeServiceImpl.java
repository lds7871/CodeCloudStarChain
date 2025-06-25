package com.mojian.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojian.common.Constants;
import com.mojian.common.RedisConstants;
import com.mojian.common.Result;
import com.mojian.entity.SysNotice;
import com.mojian.mapper.SysNoticeMapper;
import com.mojian.service.HomeService;
import com.mojian.entity.SysWebConfig;
import com.mojian.mapper.SysWebConfigMapper;
import com.mojian.utils.IpUtil;
import com.mojian.utils.RedisUtil;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import cn.hutool.core.date.DateUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final SysWebConfigMapper sysWebConfigMapper;

    private final RedisUtil redisUtil;

    private final SysNoticeMapper noticeMapper;

    @Override
    public Result<SysWebConfig> getWebConfig() {

        SysWebConfig sysWebConfig = new SysWebConfig();
        Object value = redisUtil.get(RedisConstants.WEB_CONFIG_KEY);
        if (value == null) {
            LambdaQueryWrapper<SysWebConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.last("limit 1");
            sysWebConfig = sysWebConfigMapper.selectOne(wrapper);
        }else {
            sysWebConfig = JSONObject.parseObject(value.toString(), SysWebConfig.class);
        }

        //获取浏览量和访问量
        long blogViewsCount = 0;
        long visitorCount = 0;
        if (redisUtil.hasKey(RedisConstants.BLOG_VIEWS_COUNT)) {
            blogViewsCount = Long.parseLong(redisUtil.get(RedisConstants.BLOG_VIEWS_COUNT).toString());
        }
        if (redisUtil.hasKey(RedisConstants.UNIQUE_VISITOR_COUNT)) {
            visitorCount = Long.parseLong(redisUtil.get(RedisConstants.UNIQUE_VISITOR_COUNT).toString());
        }

        return Result.success(sysWebConfig).putExtra("blogViewsCount", blogViewsCount).putExtra("visitorCount", visitorCount);
    }

    @Override
    public JSONObject getHotSearch(String type) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("access-key", "f94be500c45148bc185be24a38c04ad3");
        paramMap.put("secret-key", "27563ca627d5db0d57e831ca4de0f75f");
        String url = "https://www.coderutil.com/api/resou/v1/" + type;
        String result= HttpUtil.get(url, paramMap);
        return com.alibaba.fastjson2.JSONObject.parseObject(result);
    }

    @Override
    public void report() {
        // 获取ip
        String ipAddress = IpUtil.getIp();
        // 通过浏览器解析工具类UserAgent获取访问设备信息
        UserAgent userAgent = IpUtil.getUserAgent(Objects.requireNonNull(IpUtil.getRequest()));
        Browser browser = userAgent.getBrowser();
        OperatingSystem operatingSystem = userAgent.getOperatingSystem();
        // 生成唯一用户标识
        String uuid = ipAddress + browser.getName() + operatingSystem.getName();
        String md5 = DigestUtils.md5DigestAsHex(uuid.getBytes());
        
        // 获取当前日期
        String today = DateUtil.format(new Date(), "yyyy-MM-dd");
        
        // 判断是否访问
        if (!redisUtil.sIsMember(RedisConstants.UNIQUE_VISITOR, md5)) {
            // 访客量+1
            redisUtil.increment(RedisConstants.UNIQUE_VISITOR_COUNT, 1);
            // 保存唯一标识
            redisUtil.sAdd(RedisConstants.UNIQUE_VISITOR, md5);
            
            // 记录每日访问量
            String visitKey = RedisConstants.BLOG_VISIT_DAY + today;
            redisUtil.increment(visitKey, 1);
            // 设置过期时间为1年，确保数据不会丢失
            if (redisUtil.getExpire(visitKey, TimeUnit.SECONDS) == -1) {
                redisUtil.expire(visitKey, 365, TimeUnit.DAYS);
            }
        }
        
        // 总访问量+1
        redisUtil.increment(RedisConstants.BLOG_VIEWS_COUNT, 1);
        
        // 每日浏览量+1
        String viewKey = RedisConstants.BLOG_VIEW_DAY + today;
        redisUtil.increment(viewKey, 1);
        // 设置过期时间为1年，确保数据不会丢失
        if (redisUtil.getExpire(viewKey, TimeUnit.SECONDS) == -1) {
            redisUtil.expire(viewKey, 365, TimeUnit.DAYS);
        }
    }

    @Override
    public Map<String, List<SysNotice>> getNotice() {

        List<SysNotice> sysNotices = noticeMapper.selectList(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getIsShow, Constants.YES));
        return sysNotices.stream()
                .collect(Collectors.groupingBy(SysNotice::getPosition));
    }
}
