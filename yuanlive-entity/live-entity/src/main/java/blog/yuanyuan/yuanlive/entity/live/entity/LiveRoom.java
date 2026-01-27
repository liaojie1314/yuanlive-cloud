package blog.yuanyuan.yuanlive.entity.live.entity;


import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LiveRoom {
    @Schema(description="房间主键ID")
    private Long id;
    @Schema(description="主播ID (对应user库id)")
    private Long anchorId;
    @Schema(description="直播间标题")
    private String title;
    @Schema(description="直播间封面图URL")
    private String coverImg;
    @Schema(description="直播状态 0:未开播 1:直播中")
    private Integer roomStatus;
    @Schema(description="当前在线人数")
    private Integer viewCount;
    @Schema(description="分类ID")
    private Integer categoryId;
    @Schema(description="最近一次开播时间")
    private Date lastStartTime;
    @Schema(description="创建时间")
    private Date createTime;
    @Schema(description="更新时间")
    private Date updateTime;
}
