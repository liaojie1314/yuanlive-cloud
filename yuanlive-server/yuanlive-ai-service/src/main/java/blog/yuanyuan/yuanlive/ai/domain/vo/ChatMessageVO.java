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
    private String role;
    private Object content; // 因为 content 可能是字符串也可能是 BSON 对象
    private String thinking;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date timestamp;
    private Map<String, Object> metadata;
}
