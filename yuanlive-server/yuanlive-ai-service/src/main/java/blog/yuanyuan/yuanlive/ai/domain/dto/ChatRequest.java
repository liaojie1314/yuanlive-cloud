package blog.yuanyuan.yuanlive.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {

    // 会话 ID：用于关联历史上下文
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;

    // 客户端生成的消息 ID：用于前端乐观更新及后端去重
    @NotBlank(message = "客户端消息ID不能为空")
    private String clientMsgId;

    /**
     * 输入内容：目前为纯文字
     * TODO: 适配 MultipartFile 或文件 URL 列表用于多模态输入
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    // 聊天配置选项
    private ChatOptions options;

    // TODO 外部工具执行结果回传 (用于手动 Tool Use 模式)
    private List<Object> toolResults;

    // TODO 特定 Agent 的高级设置
    private Map<String, Object> agentSettings;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatOptions {

        /**
         * 是否开启联网搜索 (MCP SearXNG/Google Search 等)
         */
        @Builder.Default
        private boolean useNetwork = false;

        /**
         * 是否开启深度推理功能 (针对支持 Reasoning 的模型如 DeepSeek-R1)
         */
        @Builder.Default
        private boolean useReasoning = true;

        // 选择的模型名称
        // 默认 "auto"，后端根据 useReasoning 自动路由
        @Builder.Default
        private String model = "auto";

        /**
         * 采样温度：控制随机性 (0.0 - 1.0)
         */
        private Double temperature;

        /**
         * 最大生成长度
         */
        private Integer maxTokens;
    }
}
