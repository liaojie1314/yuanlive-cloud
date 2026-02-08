package blog.yuanyuan.yuanlive.entity.user.entity;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import me.ahoo.cosid.annotation.CosId;

@Data
@SuperBuilder
@NoArgsConstructor
public class UserFollow {
    @Schema(description="主键ID")
    @TableId(type = IdType.INPUT)
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
    @Schema(description = "上次观看该作者的最新视频ID")
    private Long lastReadVideoId;
}
