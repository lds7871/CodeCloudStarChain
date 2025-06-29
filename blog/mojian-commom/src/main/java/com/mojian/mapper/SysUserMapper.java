package com.mojian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mojian.entity.Users;
import com.mojian.vo.user.UsersVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.aspectj.apache.bcel.classfile.Module;

@Mapper
public interface SysUserMapper extends BaseMapper<Users> {
    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Users selectByUsername(@Param("username") String username);

    IPage<UsersVo> selectUserPage(@Param("page") Page<Object> page, @Param("sysUser") Users sysUser);

    Double selectBalcanceByUser(@Param("userId") Long userId);

    /**
     * gitee登录时，使用唯一的UniqueId 进行判断是否存在该用户的信息
     *
     * @param id UniqueId
     * @return 用户信息
     */
    Users selectByUniqueId(@Param("uniqueId") Long id);

    Users getUserInfo(@Param("userId") Long userId);

    Integer updateUserInfo(@Param("users") Users user);

    Users selectByEmail(@Param("email") String email);

    Users login(@Param("openId") String openId);
}
