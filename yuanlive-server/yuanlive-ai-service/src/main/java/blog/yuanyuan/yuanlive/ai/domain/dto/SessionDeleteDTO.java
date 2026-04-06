package blog.yuanyuan.yuanlive.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "会话删除DTO")
public class SessionDeleteDTO {
    @Schema(description = "会话ID")
    @NotEmpty(message = "删除列表不能为空")
    @Size(max = 100, message = "单次删除不能超过100条")
    private List<String> conversationIds;
}
