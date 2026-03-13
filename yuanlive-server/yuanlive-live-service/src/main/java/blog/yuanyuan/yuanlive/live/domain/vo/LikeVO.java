package blog.yuanyuan.yuanlive.live.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class LikeVO {
    @Schema(description = "是否点赞")
    private boolean isLiked;
    @Schema(description = "点赞数")
    private Long count;
}
