package blog.yuanyuan.yuanlive.user.domain.vo;

import blog.yuanyuan.yuanlive.entity.user.entity.UserFollow;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@Schema(description = "用户关注信息VO")
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = {"updateTime"})
public class UserFollowVO extends UserFollow {
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像地址")
    private String avatar;
}