package blog.yuanyuan.yuanlive.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户关注请求DTO")
public class UserFollowDTO {

    @Schema(description = "被关注用户的ID")
    @NotNull(message = "被关注用户的ID不能为空")
    private Long followUserId;

    @Schema(description = "当前操作用户的ID")
    private Long userId;
}