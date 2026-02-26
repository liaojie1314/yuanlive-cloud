package blog.yuanyuan.yuanlive.live.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class VideoVO {
    @Schema(description="视频ID")
    private Long id;
    @Schema(description="回放视频地址")
    private String videoUrl;
    @Schema(description="视频封面")
    private String coverUrl;
    @Schema(description="点赞总数")
    private Integer likeCount;
    @Schema(description="评论总数")
    private Integer commentCount;
    @Schema(description="分享数")
    private Integer shareCount;
    @Schema(description="收藏数")
    private Integer collectCount;
    @Schema(description="视频类型 0 -> 录播 1-> 上传视频")
    private Integer type;
    @Schema(description="是否观看过")
    private boolean watched;
    @Schema(description="视频描述")
    private String description;
}
