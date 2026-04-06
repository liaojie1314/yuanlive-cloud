package blog.yuanyuan.yuanlive.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
public class AiSessionVO {
    private String conversationId;
    private String title;
    // 格式：yyyy-MM-dd
    @Field("lastUpdateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime timestamp;
    private Boolean isTop;
}
