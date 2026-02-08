package blog.yuanyuan.yuanlive.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserFollowUnseenVO {
    @Schema(description = "被关注用户的用户名")
    private String username;
    @Schema(description = "头像地址")
    private String avatar;
    @Schema(description = "未观看的视频数")
    private Integer unseenCount;
}
