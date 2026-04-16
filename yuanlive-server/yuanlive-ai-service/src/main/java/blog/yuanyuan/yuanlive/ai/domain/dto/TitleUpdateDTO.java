package blog.yuanyuan.yuanlive.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "修改AI会话标题DTO")
public class TitleUpdateDTO {
    @Schema(description = "会话ID")
    @NotBlank(message = "会话ID不能为空")
    private String conversationId;
    @Schema(description = "新的会话标题")
    @NotBlank(message = "新的会话标题不能为空")
    private String title;
}
