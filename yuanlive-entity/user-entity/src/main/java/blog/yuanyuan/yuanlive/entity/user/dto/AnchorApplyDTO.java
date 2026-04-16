package blog.yuanyuan.yuanlive.entity.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "主播申请DTO")
public class AnchorApplyDTO {
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "身份证号")
    private String idCard;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "默认子分区的id")
    private Integer categoryId;

    @Schema(description = "申请理由")
    private String reason;
}
