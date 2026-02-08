package blog.yuanyuan.yuanlive.user.domain.vo;

import blog.yuanyuan.yuanlive.entity.user.entity.SysRole;
import blog.yuanyuan.yuanlive.entity.user.entity.SysUser;
import blog.yuanyuan.yuanlive.entity.user.entity.UserStats;
import blog.yuanyuan.yuanlive.entity.user.enums.GenderEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户VO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户VO")
@JsonIgnoreProperties(value = {"password"})
public class UserVO extends SysUser {
    @Schema(description = "角色列表")
    private List<SysRole> roles;
    @Schema(description = "设备信息")
    private String device;
    @Schema(description = "用户统计信息")
    @JsonIgnoreProperties(value = {"videoCount"})
    private UserStats userStats;
}
