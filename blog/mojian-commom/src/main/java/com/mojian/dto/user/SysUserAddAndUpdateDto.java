package com.mojian.dto.user;

import com.mojian.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "新增用户参数")
public class SysUserAddAndUpdateDto {

    @Schema(description = "用户信息")
    private Users user;

    @Schema(description = "角色ID列表")
    private List<Integer> roleIds;
}
