package blog.yuanyuan.yuanlive.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "二维码信息")
public class QrCodeVO {
    @Schema(description = "二维码唯一标识 (用于轮询状态)")
    private String uuid;

    @Schema(description = "二维码内容 (用于生成图片，协议格式：yuanlive://...)")
    private String content;
}
