package blog.yuanyuan.yuanlive.user.mapper;

import blog.yuanyuan.yuanlive.entity.user.entity.SysRole;
import blog.yuanyuan.yuanlive.user.domain.vo.RoleVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author frodepu
* @description 针对表【sys_role(角色表)】的数据库操作Mapper
* @createDate 2025-12-26 15:51:16
* @Entity blog.yuanyuan.yuanlive.entity.user.entity.SysRole
*/
public interface SysRoleMapper extends BaseMapper<SysRole> {
    RoleVO getRoleVOByID(Long roleId);
}




