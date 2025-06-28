package com.mojian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.dto.user.WeChatInfo;
import com.mojian.entity.SysArticle;
import com.mojian.entity.SysComment;
import com.mojian.entity.Users;
import com.mojian.mapper.*;
import com.mojian.service.UserService;
import com.mojian.utils.PageUtil;
import com.mojian.vo.article.ArticleListVo;
import com.mojian.vo.comment.CommentListVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: quequnlong
 * @date: 2025/1/11
 * @description:
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private  SysUserMapper sysUserMapper;

    private final SysCommentMapper commentMapper;

    private final SysArticleMapper articleMapper;
    private RedisTemplate<Users, Object> redisTemplate;
    @Autowired
    private WeChatMapper weChatMapper;
    private final SysTagMapper tagMapper;
    @Autowired
    private GiteeMapper giteeMapper;

    @Override
    public IPage<CommentListVo> selectMyComment() {
        return commentMapper.selectMyComment(PageUtil.getPage(), StpUtil.getLoginIdAsLong());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void delMyComment(List<Long> ids) {
        commentMapper.deleteBatchIds(ids);
        commentMapper.delete(new LambdaQueryWrapper<SysComment>()
                .in(SysComment::getParentId, ids));
        return null;
    }

    @Override
    public IPage<ArticleListVo> selectMyLike() {
        return articleMapper.selectMyLike(PageUtil.getPage(),StpUtil.getLoginIdAsLong());
    }

    @Override
    public IPage<CommentListVo> getMyReply() {
        return commentMapper.getMyReply(PageUtil.getPage(),StpUtil.getLoginIdAsLong());
    }

    @Override
    public void updateProfile(Users user) {
        sysUserMapper.updateById(user);
    }

    @Override
    public void updatewxProfile(WeChatInfo weChatInfo) {
        weChatMapper.updateById(weChatInfo);
    }


    @Override
    public IPage<ArticleListVo> selectMyArticle(SysArticle article) {
        article.setUserId(StpUtil.getLoginIdAsLong());
        return articleMapper.selectMyArticle(PageUtil.getPage(),article);
    }

    @Override
    public Integer selectMyBalance(Integer userId) {
        // 简化逻辑：直接从sys_user表查询用户余额
        // 所有用户类型的余额信息都应该同步到sys_user表中
        return sysUserMapper.selectBalcanceByUser(userId);
    }
}
