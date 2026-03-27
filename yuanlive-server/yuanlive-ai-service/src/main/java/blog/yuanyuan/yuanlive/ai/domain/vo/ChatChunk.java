package blog.yuanyuan.yuanlive.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatChunk {

    // ================= ID 追踪 =================
    private String clientMsgId; // 客户端传来
    private String userMsgId;   // 后端生成
    private String aiMsgId;     // 后端生成

    // ================= 思考过程 (Streaming) =================
    private String thinking;

    /**
     * 思考总耗时 (通常在思考结束或 done 状态时返回)
     */
    private Long thinkingTime;

    // ================= 回答内容 =================
    /**
     * content 可以是文字，也可以是包含多媒体的对象
     * 这里的 ContentBody 兼容了流式文字和静态资源
     */
    private ContentBody content;

    // ================= 状态控制 =================
    /**
     * streaming | done
     */
    private String status;
    private List<Object> toolCalls; // TODO: 待实现的工具调用结构
    private List<Object> citations; // TODO: 待实现的引用/参考文献

    // ------------------- 内部结构类 -------------------
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentBody {
        private String text;          // 文字内容 (流式)
        private List<String> images;  // 图片URL数组
        private List<String> audios;  // 音频URL数组
        private List<String> videos;  // 视频URL数组
    }

    // ------------------- 快速构建静态方法 -------------------

    /**
     * 构建思考中的 Chunk
     */
    public static ChatChunk reasoning(String thinking, String clientMsgId, String userMsgId, String aiMsgId) {
        return ChatChunk.builder()
                .clientMsgId(clientMsgId)
                .userMsgId(userMsgId)
                .aiMsgId(aiMsgId)
                .thinking(thinking)
                .status("streaming")
                .build();
    }

    /**
     * 构建文字内容增量的 Chunk
     */
    public static ChatChunk text(String text, String clientMsgId, String userMsgId, String aiMsgId) {
        return ChatChunk.builder()
                .clientMsgId(clientMsgId)
                .userMsgId(userMsgId)
                .aiMsgId(aiMsgId)
                .content(ContentBody.builder().text(text).build())
                .status("streaming")
                .build();
    }

    /**
     * 构建完成状态的 Chunk (可携带最终资源)
     */
    public static ChatChunk done(String clientMsgId, String userMsgId, String aiMsgId, ContentBody finalContent, Long time) {
        return ChatChunk.builder()
                .clientMsgId(clientMsgId)
                .userMsgId(userMsgId)
                .aiMsgId(aiMsgId)
                .content(finalContent)
                .thinkingTime(time)
                .status("done")
                .build();
    }
}