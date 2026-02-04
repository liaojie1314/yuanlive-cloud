package blog.yuanyuan.yuanlive.entity.live.entity;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import me.ahoo.cosid.annotation.CosId;

@Data
public class LiveRecord {
    @Schema(description="记录ID")
    @TableId(type = IdType.INPUT)
    private Long id;
    @Schema(description="主播ID")
    private Long anchorId;
    @Schema(description="直播间ID")
    private Long roomId;
    @Schema(description="开播时间")
    private Date startTime;
    @Schema(description="关播时间")
    private Date endTime;
    @Schema(description="本场最高在线人数")
    private Integer peakViewers;
    @Schema(description="本场累计观看人次")
    private Integer watchCount;
    @Schema(description="回放视频地址")
    private String videoUrl;
}
