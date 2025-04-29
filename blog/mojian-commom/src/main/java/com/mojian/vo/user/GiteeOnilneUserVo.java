package com.mojian.vo.user;

import com.mojian.dto.user.GiteeInfo;
import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * @className: GiteeOnilneUserVo
 * @author: Icw
 * @date: 2025/4/28 23:52
 * @Version: 1.0
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GiteeOnilneUserVo extends GiteeInfo {
    private String tokenValue;
}
