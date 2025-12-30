package blog.yuanyuan.yuanlive.entity.user.entity;


import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysUser {
    @Schema(description="用户ID")
    private Long uid;
    @Schema(description="用户名")
    private String username;
    @Schema(description="密码(BCrypt加密)")
    private String password;
    @Schema(description="头像")
    private String avatar;
    @Schema(description="手机号")
    private String phone;
    @Schema(description="邮箱")
    private String email;
    @Schema(description="用户类型")
    private UserRoleEnum role;
    @Schema(description="状态:1-正常,0-停用")
    private Integer status;
    @Schema(description="逻辑删除标志:0-存在,1-删除")
    private Integer delFlag;
    @Schema(description="")
    private Date createTime;
    @Schema(description="")
    private Date updateTime;
}
