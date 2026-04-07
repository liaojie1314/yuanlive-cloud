package blog.yuanyuan.yuanlive.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Data
@Builder
@Schema(description = "AI聊天信息")
public class ChatMessageVO {
    
    @Schema(description = "客户端消息ID")
    private String clientId;
    
    @Schema(description = "用户ID")
    private String userId;
    
    @Schema(description = "AI ID")
    private String aiId;
    
    @Schema(description = "角色:user或assistant")
    private String role;
    
    @Schema(description = "发送者(用户名/AI名/Agent名)")
    private String sender;
    
    @Schema(description = "头像")
    private String avatar;
    
    @Schema(description = "消息内容")
    private Object content;
    
    @Schema(description = "时间")
    private Long time;
    
    @Schema(description = "思考过程(AI专有)")
    private String thinking;
}
