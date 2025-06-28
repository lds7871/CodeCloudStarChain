package com.mojian.service;

import com.mojian.vo.dashboard.IndexVo;
import com.mojian.vo.dashboard.VisitTrendVo;

import java.util.List;
import java.util.Map;


public interface IndexService {

    /**
     * 首页获取顶部数据
     *
     * @return
     */
    IndexVo index();

    /**
     * 获取分类
     *
     * @return
     */
    List<Map<String, Integer>> getCategories();

    /**
     * 获取访问趋势数据
     * 
     * @param type 类型：week-本周，month-本月
     * @return 访问趋势数据
     */
    VisitTrendVo getVisitTrend(String type);
}
