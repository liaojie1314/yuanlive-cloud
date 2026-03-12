package blog.yuanyuan.yuanlive.entity.live.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserVideoInteract {
    @Schema(description="")
    private Long id;
    @Schema(description="用户id")
    private Long userId;
    @Schema(description="视频id")
    private Long videoId;
    @Schema(description="是否点赞")
    private Integer hasLiked;
    @Schema(description = "是否不喜欢")
    private Integer hasUnliked;
    @Schema(description="是否分享")
    private Integer hasShared;
    @Schema(description="是否推荐")
    private Integer hasRecommended;
}
