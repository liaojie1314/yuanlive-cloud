package blog.yuanyuan.yuanlive.entity.user.entity;


import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysRole {
    @Schema(description="")
    @TableId
    private Long roleId;
    @Schema(description="角色名称(汉字)")
    private String roleName;
    @Schema(description="角色权限字符串(如 admin)")
    private String roleKey;
    @Schema(description="状态:1-正常,0-停用")
    private Integer status;
    @Schema(description="")
    private Date createTime;
    @Schema(description="")
    private Date updateTime;
}
