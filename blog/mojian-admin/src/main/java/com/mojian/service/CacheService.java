package com.mojian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.vo.cache.CacheInfoVo;
import com.mojian.vo.cache.CacheKeyQuery;
import com.mojian.vo.cache.CacheKeyVo;
import com.mojian.vo.cache.CacheMemoryVo;

public interface CacheService {

    /**
     * 获取缓存基本信息
     */
    CacheInfoVo getCacheInfo();

    /**
     * 获取内存信息
     */
    CacheMemoryVo getMemoryInfo();

    /**
     * 获取缓存键列表
     */
    IPage<CacheKeyVo> getKeyList(CacheKeyQuery query);

    /**
     * 清空缓存
     */
    void clearCache();
} 