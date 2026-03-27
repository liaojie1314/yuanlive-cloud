package blog.yuanyuan.yuanlive.ai.controller;

import blog.yuanyuan.yuanlive.ai.domain.dto.ChatRequest;
import blog.yuanyuan.yuanlive.ai.domain.vo.ChatChunk;
import blog.yuanyuan.yuanlive.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/chat")
@Tag(name = "AI聊天管理")
public class AiChatController {
    @Resource
    private AiChatService aiChatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天")
    public Flux<ChatChunk> streamChat(@RequestBody ChatRequest request) {
        return aiChatService.streamChat(request);
    }
}
