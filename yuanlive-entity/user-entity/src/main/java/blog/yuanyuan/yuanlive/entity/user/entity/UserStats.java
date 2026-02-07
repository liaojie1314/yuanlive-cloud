package blog.yuanyuan.yuanlive.entity.user.entity;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户统计信息")
public class UserStats {
    @Schema(description="")
    @JsonIgnore
    @TableId
    private Long userId;
    @Schema(description="关注数")
    private Integer followingCount;
    @Schema(description="粉丝数")
    private Integer followerCount;
    @Schema(description="获赞总数")
    private Integer totalLikesReceived;
    @Schema(description="视频总数")
    private Integer videoCount;
    @Schema(description = "关注的人正在直播的人数")
    @TableField(exist = false)
    private Integer followingLiveCount;
    @Schema(description="")
    private Date updateTime;
}
