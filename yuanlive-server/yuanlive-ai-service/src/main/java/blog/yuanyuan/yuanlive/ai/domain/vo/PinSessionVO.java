package blog.yuanyuan.yuanlive.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "置顶AI会话返回结果")
public class PinSessionVO {
    @Schema(description = "是否置顶")
    private Boolean isPin;
}
