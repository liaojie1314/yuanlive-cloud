package blog.yuanyuan.yuanlive.entity.user.vo;

import blog.yuanyuan.yuanlive.entity.user.entity.UserFollow;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Schema(description = "用户关注信息VO")
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(value = {"updateTime"})
public class UserFollowVO extends UserFollow {
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像地址")
    private String avatar;
}