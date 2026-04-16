package blog.yuanyuan.yuanlive.entity.live.entity;


import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class VideoResource {
    @Schema(description="视频ID")
    private Long id;
    @Schema(description="用户ID")
    private Long userId;
    @Schema(description = "视频所属分类(录播与上传通用)")
    private Integer categoryId;
    @Schema(description="直播间ID")
    private Long roomId;
    @Schema(description="视频时长")
    private Long duration;
    @Schema(description="视频标题（录播可默认为“直播回放+日期”")
    private String title;
    @Schema(description="开播时间")
    private Date startTime;
    @Schema(description="关播时间(录播)")
    private Date endTime;
    @Schema(description="本场最高在线人数")
    private Integer peakViewers;
    @Schema(description="本场累计观看人次")
    private Integer watchCount;
    @Schema(description="回放视频地址")
    private String videoUrl;
    @Schema(description="视频封面")
    private String coverUrl;
    @Schema(description="点赞总数")
    private Integer likeCount;
    @Schema(description="评论总数")
    private Integer commentCount;
    @Schema(description="播放次数")
    private Integer viewCount;
    @Schema(description="分享数")
    private Integer shareCount;
    @Schema(description="收藏数")
    private Integer collectCount;
    @Schema(description="视频类型 0 -> 录播 1-> 上传视频")
    private Integer type;
    @Schema(description="上传时间")
    private Date createTime;
    @Schema(description="视频描述信息")
    private String description;
}
