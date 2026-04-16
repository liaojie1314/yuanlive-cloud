package blog.yuanyuan.yuanlive.ai.service;

import blog.yuanyuan.yuanlive.ai.domain.dto.ChatRequest;
import blog.yuanyuan.yuanlive.ai.domain.vo.ChatChunk;
import reactor.core.publisher.Flux;

public interface AiChatService {
    Flux<ChatChunk> streamChat(ChatRequest chatRequest);
}
