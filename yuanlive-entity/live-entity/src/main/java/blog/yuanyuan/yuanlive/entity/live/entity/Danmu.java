package blog.yuanyuan.yuanlive.entity.live.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
public class Danmu {

    @Schema(description="弹幕主键ID")
    @TableId(type = IdType.AUTO) // 这个最好保留，告诉框架数据库是自增主键，否则它会默认生成超长雪花算法ID
    private Long id;
    @Schema(description="关联的视频ID")
    private Long videoId;
    @Schema(description="发送弹幕的用户ID")
    private Long userId;
    @Schema(description="弹幕内容")
    private String content;
    @Schema(description="弹幕在视频中的时间点(单位:秒)")
    private Integer timeOffset;
    @Schema(description="弹幕位置: scroll, top, bottom")
    private String position;
    @Schema(description="弹幕点赞数")
    private Integer likeCount;
    @Schema(description="状态: 0正常, 1被举报隐藏")
    private Integer status;
    @Schema(description="发送时间")
    private Date createTime;
}