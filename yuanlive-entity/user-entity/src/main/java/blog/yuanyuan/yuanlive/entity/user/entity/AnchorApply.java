package blog.yuanyuan.yuanlive.entity.user.entity;


import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnchorApply {
    @Schema(description="主键ID")
    private Long id;
    @Schema(description="申请人ID")
    private Long uid;
    @Schema(description="真实姓名")
    private String realName;
    @Schema(description="身份证号")
    private String idCard;
    @Schema(description="联系手机号")
    private String phone;
    @Schema(description="所属分区ID(子分区)")
    private Integer categoryId;
    @Schema(description="申请理由")
    private String reason;
    @Schema(description="审核状态: 0-待审核, 1-已通过, 2-被驳回")
    private Integer status;
    @Schema(description="审核意见")
    private String auditRemark;
}
