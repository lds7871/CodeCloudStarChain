package com.mojian.controller.system;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.Flowable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.mojian.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @className: SysAIController
 * @author: Icw
 * @date: 2025/6/29 23:37
 * @Version: 1.0
 * @description: 阿里云百炼AI对话控制器
 */
@RestController
@RequestMapping("/ai")
@Tag(name = "AI对话管理")
@Slf4j
public class SysAIController {

    @Value("${AI_AliYun:}")
    private String apiKey;

    @Value("${ai.model:qwen-plus}")
    private String model;
    
    /**
     * 控制器初始化时设置API Key
     */
    @jakarta.annotation.PostConstruct
    public void initApiKey() {
        String currentApiKey = getApiKeyForInit();
        if (currentApiKey != null && !currentApiKey.trim().isEmpty()) {
            System.setProperty("DASHSCOPE_API_KEY", currentApiKey);
            System.setProperty("ALIBABA_CLOUD_ACCESS_KEY_ID", currentApiKey);
            log.info("SysAIController初始化，设置全局API Key: {}", currentApiKey.substring(0, Math.min(10, currentApiKey.length())) + "...");
        } else {
            log.error("SysAIController初始化失败：API Key未配置！");
        }
    }
    
    private String getApiKeyForInit() {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey;
        }
        String envApiKey = System.getenv("AI_AliYun");
        if (envApiKey != null && !envApiKey.trim().isEmpty()) {
            return envApiKey;
        }
        String dashscopeKey = System.getenv("DASHSCOPE_API_KEY");
        if (dashscopeKey != null && !dashscopeKey.trim().isEmpty()) {
            return dashscopeKey;
        }
        return null;
    }

    /**
     * AI对话请求DTO
     */
    @Data
    public static class AIChatRequest {
        private String message;
        private String sessionId; // 可选的会话ID，用于多轮对话
        private Boolean newSession = false; // 是否开始新会话
    }

    /**
     * AI对话响应DTO
     */
    @Data
    public static class AIChatResponse {
        private String reply;
        private String sessionId;
        private Integer messageCount; // 当前会话的消息数量
    }

    /**
     * 会话状态响应DTO
     */
    @Data
    public static class SessionStatusResponse {
        private boolean hasSession;
        private int messageCount;
        private String sessionId;
        
        public SessionStatusResponse(boolean hasSession, int messageCount, String sessionId) {
            this.hasSession = hasSession;
            this.messageCount = messageCount;
            this.sessionId = sessionId;
        }
    }

    /**
     * 单次AI对话接口
     */
    @SaCheckLogin
    @PostMapping("/chat")
    @Operation(summary = "AI单次对话")
    public Result<AIChatResponse> chat(@RequestBody AIChatRequest request, HttpSession httpSession) {
        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return Result.error("消息内容不能为空");
            }

            // 获取或创建会话消息列表
            String currentSessionId = request.getSessionId() != null ? request.getSessionId() : httpSession.getId();
            String sessionKey = "ai_messages_" + currentSessionId;
            
            @SuppressWarnings("unchecked")
            List<Message> messages = (List<Message>) httpSession.getAttribute(sessionKey);
            
            if (messages == null || request.getNewSession()) {
                messages = new ArrayList<>();
                messages.add(createMessage(Role.SYSTEM, "你是码云星链的AI客服，请你根据码云星链客服的身份，使用中文并且礼貌的回答用户的问题，在第一次对话中，要以我是码云星链的AI客服，有什么可以帮助您开头"));
            }

            // 添加用户消息
            messages.add(createMessage(Role.USER, request.getMessage()));

            // 创建生成参数
            GenerationParam param = createGenerationParam(messages);
            
            // 调用AI服务
            GenerationResult result = callGenerationWithMessages(param);
            
            if (result == null || result.getOutput() == null || 
                result.getOutput().getChoices() == null || 
                result.getOutput().getChoices().isEmpty()) {
                return Result.error("AI服务返回结果为空");
            }

            // 获取AI回复
            Message aiMessage = result.getOutput().getChoices().get(0).getMessage();
            String aiReply = aiMessage.getContent();
            
            // 将AI回复添加到对话历史
            messages.add(aiMessage);
            
            // 保存对话历史到session（保留最近100轮对话，每轮2条消息，共200条+1条系统消息=201条）
            if (messages.size() > 201) { // 100轮对话 + 系统消息
                List<Message> trimmed = new ArrayList<>();
                trimmed.add(messages.get(0)); // 保留系统消息
                // 保留最近200条消息（100轮对话）
                trimmed.addAll(messages.subList(messages.size() - 200, messages.size()));
                messages = trimmed;
            }
            httpSession.setAttribute(sessionKey, messages);

            // 构造响应
            AIChatResponse response = new AIChatResponse();
            response.setReply(aiReply);
            response.setSessionId(currentSessionId);
            response.setMessageCount(messages.size() - 1); // 减去系统消息

            log.info("AI对话成功，会话ID：{}，消息数：{}", response.getSessionId(), response.getMessageCount());
            
            return Result.success(response);
            
        } catch (NoApiKeyException e) {
            log.error("AI API Key未配置或无效", e);
            return Result.error("AI服务配置错误：API Key未配置或无效");
        } catch (ApiException e) {
            log.error("AI API调用异常", e);
            return Result.error("AI服务调用失败：" + e.getMessage());
        } catch (InputRequiredException e) {
            log.error("AI API输入参数异常", e);
            return Result.error("请求参数错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("AI对话异常", e);
            return Result.error("AI对话服务异常：" + e.getMessage());
        }
    }

    /**
     * 流式AI对话接口
     */
    @SaCheckLogin
    @PostMapping("/chatStream")
    @Operation(summary = "AI流式对话")
    public SseEmitter chatStream(@RequestBody AIChatRequest request, HttpSession httpSession) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        
        // 在异步线程中处理AI请求
        new Thread(() -> {
            try {
                if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                    emitter.send(SseEmitter.event().name("error").data("消息内容不能为空"));
                    emitter.complete();
                    return;
                }

                // 获取或创建会话消息列表
                String currentSessionId = request.getSessionId() != null ? request.getSessionId() : httpSession.getId();
                String sessionKey = "ai_messages_" + currentSessionId;
                
                @SuppressWarnings("unchecked")
                List<Message> messages = (List<Message>) httpSession.getAttribute(sessionKey);
                
                if (messages == null || request.getNewSession()) {
                    messages = new ArrayList<>();
                    messages.add(createMessage(Role.SYSTEM, "你是码云星链的AI客服，请你根据码云星链客服的身份，使用中文并且礼貌的回答用户的问题，在第一次对话中，要以我是码云星链的AI客服，有什么可以帮助您开头"));
                }

                // 添加用户消息
                messages.add(createMessage(Role.USER, request.getMessage()));

                // 创建生成参数，启用流式输出
                GenerationParam param = createStreamGenerationParam(messages);
                
                // 调用流式AI服务
                Flowable<GenerationResult> resultFlow = callStreamGenerationWithMessages(param);
                
                StringBuilder fullResponse = new StringBuilder();

                List<Message> finalMessages = messages;
                resultFlow.blockingForEach(result -> {
                    try {
                        if (result != null && result.getOutput() != null && 
                            result.getOutput().getChoices() != null && 
                            !result.getOutput().getChoices().isEmpty()) {
                            
                            Message aiMessage = result.getOutput().getChoices().get(0).getMessage();
                            String content = aiMessage.getContent();
                            
                            if (content != null && !content.isEmpty()) {
                                fullResponse.append(content);
                                
                                // 发送流式数据
                                AIChatResponse response = new AIChatResponse();
                                response.setReply(content);
                                response.setSessionId(currentSessionId);
                                response.setMessageCount(finalMessages.size());
                                
                                emitter.send(SseEmitter.event().name("message").data(response));
                            }
                        }
                    } catch (Exception e) {
                        log.error("发送流式数据失败", e);
                    }
                });
                
                // 保存完整对话到session
                if (fullResponse.length() > 0) {
                    messages.add(createMessage(Role.ASSISTANT, fullResponse.toString()));
                    
                    // 保存对话历史到session
                    if (messages.size() > 201) {
                        List<Message> trimmed = new ArrayList<>();
                        trimmed.add(messages.get(0)); // 保留系统消息
                        trimmed.addAll(messages.subList(messages.size() - 200, messages.size()));
                        messages = trimmed;
                    }
                    httpSession.setAttribute(sessionKey, messages);
                }
                
                emitter.send(SseEmitter.event().name("complete").data("对话完成"));
                emitter.complete();
                
                log.info("AI流式对话成功，会话ID：{}", currentSessionId);
                
            } catch (Exception e) {
                log.error("AI流式对话异常", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("AI对话服务异常：" + e.getMessage()));
                    emitter.complete();
                } catch (Exception ex) {
                    log.error("发送错误消息失败", ex);
                }
            }
        }).start();
        
        return emitter;
    }

    /**
     * 清除会话历史
     */
    @SaCheckLogin
    @PostMapping("/clearSession")
    @Operation(summary = "清除AI对话会话")
    public Result<Void> clearSession(@RequestParam(required = false) String sessionId, HttpSession httpSession) {
        try {
            String currentSessionId = sessionId != null ? sessionId : httpSession.getId();
            String sessionKey = "ai_messages_" + currentSessionId;
            httpSession.removeAttribute(sessionKey);
            log.info("AI会话已清除，会话ID：{}", currentSessionId);
            return Result.success();
        } catch (Exception e) {
            log.error("清除AI会话异常", e);
            return Result.error("清除会话失败：" + e.getMessage());
        }
    }

    /**
     * 获取会话状态
     */
    @SaCheckLogin
    @GetMapping("/sessionStatus")
    @Operation(summary = "获取AI对话会话状态")
    public Result<SessionStatusResponse> getSessionStatus(@RequestParam(required = false) String sessionId, HttpSession httpSession) {
        try {
            String currentSessionId = sessionId != null ? sessionId : httpSession.getId();
            String sessionKey = "ai_messages_" + currentSessionId;
            
            @SuppressWarnings("unchecked")
            List<Message> messages = (List<Message>) httpSession.getAttribute(sessionKey);
            
            if (messages == null) {
                return Result.success(new SessionStatusResponse(false, 0, currentSessionId));
            }
            
            return Result.success(new SessionStatusResponse(true, messages.size() - 1, currentSessionId)); // 减去系统消息
            
        } catch (Exception e) {
            log.error("获取AI会话状态异常", e);
            return Result.error("获取会话状态失败：" + e.getMessage());
        }
    }

    /**
     * 创建生成参数
     */
    private GenerationParam createGenerationParam(List<Message> messages) {
        // 检查API Key配置
        String finalApiKey = null;
        
        // 优先使用环境变量
        String envApiKey = System.getenv("AI_AliYun");
        if (envApiKey != null && !envApiKey.trim().isEmpty()) {
            finalApiKey = envApiKey;
        } else if (apiKey != null && !apiKey.trim().isEmpty()) {
            finalApiKey = apiKey;
        }
        
        if (finalApiKey == null || finalApiKey.trim().isEmpty()) {
            throw new RuntimeException("API Key未配置，请设置AI_AliYun环境变量或配置项");
        }
        
        // 检查model配置
        String finalModel = model;
        if (finalModel == null || finalModel.trim().isEmpty()) {
            finalModel = "qwen-plus"; // 默认模型
        }
        
        log.info("AI调用参数 - API Key: {}, Model: {}, Messages Count: {}", 
                finalApiKey.substring(0, Math.min(10, finalApiKey.length())) + "...", 
                finalModel, 
                messages.size());
        
        return GenerationParam.builder()
                .apiKey(finalApiKey)
                .model(finalModel)
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
    }

    /**
     * 调用AI生成服务
     */
    private GenerationResult callGenerationWithMessages(GenerationParam param) 
            throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        return gen.call(param);
    }

    /**
     * 创建流式生成参数
     */
    private GenerationParam createStreamGenerationParam(List<Message> messages) {
        String modelName = getModelName();
        String apiKey = getApiKey();
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API Key未配置，请设置AI_AliYun配置项或DASHSCOPE_API_KEY环境变量");
        }
        
        // 确保SDK能找到API Key
        System.setProperty("DASHSCOPE_API_KEY", apiKey);
        
        log.info("流式AI调用参数 - Model: {}, API Key: {}, Messages Count: {}", 
                modelName, 
                apiKey.substring(0, Math.min(10, apiKey.length())) + "...", 
                messages.size());
        
        return GenerationParam.builder()
                .apiKey(apiKey)  // 直接在参数中设置API Key
                .model(modelName)
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .incrementalOutput(true)  // 启用增量输出
                .build();
    }

    /**
     * 调用流式AI生成服务
     */
    private Flowable<GenerationResult> callStreamGenerationWithMessages(GenerationParam param) 
            throws ApiException, NoApiKeyException, InputRequiredException {
        
        // 在创建Generation实例之前，再次确保API Key设置正确
        String currentApiKey = getApiKey();
        if (currentApiKey != null && !currentApiKey.trim().isEmpty()) {
            // 同时设置多个可能的环境变量
            System.setProperty("DASHSCOPE_API_KEY", currentApiKey);
            System.setProperty("ALIBABA_CLOUD_ACCESS_KEY_ID", currentApiKey);
            
            log.info("在Generation创建前设置API Key: {}", currentApiKey.substring(0, Math.min(10, currentApiKey.length())) + "...");
        }
        
        Generation gen = new Generation();
        return gen.streamCall(param);
    }

    /**
     * 获取API Key
     */
    private String getApiKey() {
        log.info("=== API Key 获取调试信息 ===");
        log.info("配置文件 apiKey 值: [{}]", apiKey);
        log.info("环境变量 AI_AliYun: [{}]", System.getenv("AI_AliYun"));
        log.info("环境变量 DASHSCOPE_API_KEY: [{}]", System.getenv("DASHSCOPE_API_KEY"));
        
        // 优先使用配置文件的AI_AliYun
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            log.info("使用配置文件API Key: {}", apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
            return apiKey;
        }
        
        // 其次使用环境变量AI_AliYun
        String envApiKey = System.getenv("AI_AliYun");
        if (envApiKey != null && !envApiKey.trim().isEmpty()) {
            log.info("使用环境变量AI_AliYun: {}", envApiKey.substring(0, Math.min(10, envApiKey.length())) + "...");
            return envApiKey;
        }
        
        // 最后尝试标准的DASHSCOPE_API_KEY环境变量
        String dashscopeKey = System.getenv("DASHSCOPE_API_KEY");
        if (dashscopeKey != null && !dashscopeKey.trim().isEmpty()) {
            log.info("使用环境变量DASHSCOPE_API_KEY: {}", dashscopeKey.substring(0, Math.min(10, dashscopeKey.length())) + "...");
            return dashscopeKey;
        }
        
        log.error("所有API Key来源都为空！");
        return null;
    }
    
    /**
     * 获取模型名称
     */
    private String getModelName() {
        if (model != null && !model.trim().isEmpty()) {
            return model;
        }
        return "qwen-plus"; // 默认模型
    }

    /**
     * 创建消息对象
     */
    private Message createMessage(Role role, String content) {
        return Message.builder().role(role.getValue()).content(content).build();
    }
}

