package blog.yuanyuan.yuanlive.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI历史会话获取请求参数")
public class HistoryRequestDTO {
    @Schema(description = "游标(消息ID,首次进入为空)")
    private String cursor;
    
    @Schema(description = "每页数量(用户和AI消息分开算,前端一般传递偶数)")
    private int pageSize;
}
