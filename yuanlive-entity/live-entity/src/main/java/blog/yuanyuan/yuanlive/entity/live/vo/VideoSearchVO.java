package blog.yuanyuan.yuanlive.entity.live.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoSearchVO {
    @Schema(description="视频ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description="用户ID")
    private Long userId;
    @Schema(description = "直播间分类")
    private Integer categoryId;
    @Schema(description = "直播间分类名称")
    private String categoryName;
    @Schema(description="视频标题（录播可默认为“直播回放+日期”")
    private String title;
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
    @Schema(description="上传时间")
    private LocalDateTime createTime;
    @Schema(description="视频描述信息")
    private String description;
}
