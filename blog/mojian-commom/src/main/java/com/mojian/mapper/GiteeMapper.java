package com.mojian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mojian.dto.user.GiteeInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @className: GiteeMapper
 * @author: Icw
 * @date: 2025/4/28 20:32
 * @Version: 1.0
 * @description:
 */
@Mapper
public interface GiteeMapper extends BaseMapper<GiteeInfo> {
    GiteeInfo selectByUniqueId(@Param("uniqueId") Long id);
    Integer insertUserInfo(@Param("giteeInfo") GiteeInfo giteeInfo);
     void updateByUser(@Param("giteeInfo") GiteeInfo giteeInfo);
    GiteeInfo selectByUserId(@Param("userId") Integer userId);
}
