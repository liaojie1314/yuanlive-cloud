package blog.yuanyuan.yuanlive.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI历史会话分页查询参数")
public class SessionPageQueryDTO {
    @Schema(description = "页码")
    private Integer pageNum=1;
    @Schema(description = "每页大小")
    private Integer pageSize=10;
}
