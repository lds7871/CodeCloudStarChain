package com.mojian.vo.user;

import com.mojian.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户分页视图对象")
public class UsersVo extends Users {

@Schema(description = "角色id集合")
    private List<Integer> roleIds;
}
