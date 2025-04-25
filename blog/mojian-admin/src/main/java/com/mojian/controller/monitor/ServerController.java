package com.mojian.controller.monitor;

import com.mojian.common.Result;
import com.mojian.service.ServerService;
import com.mojian.vo.server.ServerInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor/server")
@Tag(name = "服务器监控")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;

    @GetMapping
    @Operation(summary = "获取服务器信息")
    public Result<ServerInfo> getServerInfo() {
        return Result.success(serverService.getServerInfo());
    }
}
