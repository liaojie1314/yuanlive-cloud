package blog.yuanyuan.yuanlive.entity.user.entity;


import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class UserFollow {
    @Schema(description="主键ID")
    private Long id;
    @Schema(description="粉丝ID (发起关注的人)")
    private Long userId;
    @Schema(description="主播ID (被关注的人)")
    private Long followUserId;
    @Schema(description="关注状态: 1-已关注, 0-已取消")
    private Integer status;
    @Schema(description="关注时间")
    private Date createTime;
    @Schema(description="更新时间")
    private Date updateTime;
}
