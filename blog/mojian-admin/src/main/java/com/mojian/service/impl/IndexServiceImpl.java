package com.mojian.service.impl;

import com.mojian.common.RedisConstants;
import com.mojian.mapper.SysArticleMapper;
import com.mojian.mapper.SysMessageMapper;
import com.mojian.mapper.SysUserMapper;
import com.mojian.service.IndexService;
import com.mojian.utils.RedisUtil;
import com.mojian.vo.dashboard.ContributionData;
import com.mojian.vo.dashboard.IndexVo;
import com.mojian.vo.dashboard.VisitTrendVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import cn.hutool.core.date.DateUtil;

import java.util.*;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final SysUserMapper sysUserMapper;

    private final SysArticleMapper sysArticleMapper;

    private final SysMessageMapper sysMessageMapper;

    private final RedisUtil redisUtil;

    @Override
    public IndexVo index() {
        Long userCount = sysUserMapper.selectCount(null);
        Long articleCount = sysArticleMapper.selectCount(null);
        Long messageCount = sysMessageMapper.selectCount(null);

        int visitCount = 0;
        Object e = redisUtil.get(RedisConstants.BLOG_VIEWS_COUNT);
        if (e != null) {
            visitCount = Integer.parseInt(e.toString());
        }

        List<ContributionData> list = sysArticleMapper.getThisYearContributionData();

        return IndexVo.builder()
                .articleCount(articleCount)
                .userCount(userCount)
                .messageCount(messageCount)
                .visitCount(visitCount)
                .contributionData(list)
                .build();
    }

    @Override
    public List<Map<String, Integer>> getCategories() {
        List<Map<String, Integer>> list = sysArticleMapper.selectCountByCategory();
        return list;
    }

    @Override
    public VisitTrendVo getVisitTrend(String type) {
        List<String> dateLabels = new ArrayList<>();
        List<Integer> visitData = new ArrayList<>();
        List<Integer> viewData = new ArrayList<>();
        
        Date today = new Date();

        // 根据类型处理不同时间段的数据
        if ("week".equals(type)) {
            // 本周数据
            for (int i = 6; i >= 0; i--) {
                Date date = DateUtil.offsetDay(today, -i);
                String dateStr = DateUtil.format(date, "yyyy-MM-dd");
                String dayOfWeek = DateUtil.format(date, "E"); // 星期几
                
                dateLabels.add(dayOfWeek);
                
                String visitKey = RedisConstants.BLOG_VISIT_DAY + dateStr;
                String viewKey = RedisConstants.BLOG_VIEW_DAY + dateStr;
                
                // 获取访问量，不存在则为0
                Object visitObj = redisUtil.get(visitKey);
                int visitCount = visitObj != null ? Integer.parseInt(visitObj.toString()) : 0;
                visitData.add(visitCount);
                
                // 获取浏览量，不存在则为0
                Object viewObj = redisUtil.get(viewKey);
                int viewCount = viewObj != null ? Integer.parseInt(viewObj.toString()) : 0;
                viewData.add(viewCount);
            }
        } else if ("month".equals(type)) {
            // 本月数据，按周统计
            Date startOfMonth = DateUtil.beginOfMonth(today);
            int totalWeeks = 4; // 默认显示4周
            
            // 计算本月第一天是星期几
            int firstDayOfWeek = DateUtil.dayOfWeek(startOfMonth) % 7; // 转换为周日为0的格式
            
            // 第一周可能不足7天，需要特殊处理
            int daysInFirstWeek = 7 - firstDayOfWeek;
            dateLabels.add("第1周");
            
            // 处理第一周的数据
            int firstWeekVisit = 0;
            int firstWeekView = 0;
            for (int i = 0; i < daysInFirstWeek; i++) {
                Date date = DateUtil.offsetDay(startOfMonth, i);
                String dateStr = DateUtil.format(date, "yyyy-MM-dd");
                
                String visitKey = RedisConstants.BLOG_VISIT_DAY + dateStr;
                String viewKey = RedisConstants.BLOG_VIEW_DAY + dateStr;
                
                Object visitObj = redisUtil.get(visitKey);
                firstWeekVisit += visitObj != null ? Integer.parseInt(visitObj.toString()) : 0;
                
                Object viewObj = redisUtil.get(viewKey);
                firstWeekView += viewObj != null ? Integer.parseInt(viewObj.toString()) : 0;
            }
            visitData.add(firstWeekVisit);
            viewData.add(firstWeekView);
            
            // 处理后续几周数据
            for (int week = 1; week < totalWeeks; week++) {
                dateLabels.add("第" + (week + 1) + "周");
                int weekVisit = 0;
                int weekView = 0;
                
                // 每周7天
                for (int i = 0; i < 7; i++) {
                    // 计算日期: 第一周天数 + (当前周-1)*7 + 当前日
                    int dayOffset = daysInFirstWeek + (week - 1) * 7 + i;
                    Date date = DateUtil.offsetDay(startOfMonth, dayOffset);
                    
                    // 如果超过当前日期，则停止计算
                    if (date.after(today)) {
                        break;
                    }
                    
                    String dateStr = DateUtil.format(date, "yyyy-MM-dd");
                    String visitKey = RedisConstants.BLOG_VISIT_DAY + dateStr;
                    String viewKey = RedisConstants.BLOG_VIEW_DAY + dateStr;
                    
                    Object visitObj = redisUtil.get(visitKey);
                    weekVisit += visitObj != null ? Integer.parseInt(visitObj.toString()) : 0;
                    
                    Object viewObj = redisUtil.get(viewKey);
                    weekView += viewObj != null ? Integer.parseInt(viewObj.toString()) : 0;
                }
                
                visitData.add(weekVisit);
                viewData.add(weekView);
            }
        }
        
        return VisitTrendVo.builder()
                .dateLabels(dateLabels)
                .visitData(visitData)
                .viewData(viewData)
                .build();
    }
}
