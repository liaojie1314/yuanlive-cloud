package blog.yuanyuan.yuanlive.entity.live.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "未看视频信息")
public class UnseenVO {
    private Long uid;
    private Integer count;
}
