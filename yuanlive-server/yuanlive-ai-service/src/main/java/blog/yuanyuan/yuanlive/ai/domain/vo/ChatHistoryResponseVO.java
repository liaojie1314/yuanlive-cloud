package blog.yuanyuan.yuanlive.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI聊天历史响应")
public class ChatHistoryResponseVO {
    @Schema(description = "是否还有更老的消息")
    private Boolean hasMore;
    
    @Schema(description = "本次返回数据中最老的一条消息ID(下次查询的cursor)")
    private String nextCursor;
    
    @Schema(description = "消息列表")
    private List<ChatMessageVO> messages;
}
