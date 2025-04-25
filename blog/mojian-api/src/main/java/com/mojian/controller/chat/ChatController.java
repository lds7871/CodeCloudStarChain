//package com.mojian.controller.chat;
//
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.mojian.common.Result;
//import com.mojian.service.ChatService;
//import com.mojian.vo.chat.ChatSendMsgVo;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/chat")
//@RequiredArgsConstructor
//@Tag(name = "门户-聊天管理")
//public class ChatController {
//
//    @Autowired
//    private final ChatService chatService;
//
//    @GetMapping("/list")
//    @Operation(summary = "获取聊天历史纪录")
//    public Result<IPage<ChatSendMsgVo>> getChatMsgList() {
//        return Result.success(chatService.getChatMsgList());
//    }
//
//    @PostMapping("/sendMsg")
//    @Operation(summary = "发送消息")
//    public Result<Void> sendMsg(@RequestBody ChatSendMsgVo chatSendMsgVo) {
//        chatService.sendMsg(chatSendMsgVo);
//        return Result.success();
//    }
//
//    @PostMapping("/recallMsg")
//    @Operation(summary = "撤回消息")
//    public Result<Void> recallMsg(@RequestBody ChatSendMsgVo chatSendMsgVo) {
//        chatService.recallMsg(chatSendMsgVo);
//        return Result.success();
//    }
//
//}
