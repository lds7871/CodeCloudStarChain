package com.mojian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.dto.user.WeChatInfo;
import com.mojian.entity.SysArticle;
import com.mojian.entity.SysUser;
import com.mojian.entity.Users;
import com.mojian.vo.article.ArticleListVo;
import com.mojian.vo.comment.CommentListVo;

import java.util.List;

/**
 * @author: quequnlong
 * @date: 2025/1/11
 * @description:
 */
public interface UserService {

    /**
     * 查询我的评论
     * @return
     */
    IPage<CommentListVo> selectMyComment();

    /**
     * 删除我的评论
     * @param ids
     * @return
     */
    Void delMyComment(List<Long> ids);

    /**
     * 查询我的点赞
     * @return
     */
    IPage<ArticleListVo> selectMyLike();

    /**
     * 查询我的回复
     * @return
     */
    IPage<CommentListVo> getMyReply();

    /**
     * 修改我的资料
     * @param user
     */
    void updateProfile(Users user);
    void updatewxProfile(WeChatInfo weChatInfo);

    /**
     * 查询我的文章
     * @return
     */
    IPage<ArticleListVo> selectMyArticle(SysArticle article);

    /**
     * 查询用户余额
     * @param userId
     * @return
     */
    Double selectMyBalance(Long userId);
}
