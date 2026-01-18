package blog.yuanyuan.yuanlive.user.service;

import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.user.entity.SysMenu;
import blog.yuanyuan.yuanlive.entity.user.entity.SysRole;
import blog.yuanyuan.yuanlive.user.domain.dto.RoleDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RoleQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.RoleVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author frodepu
* @description 针对表【sys_role(角色表)】的数据库操作Service
* @createDate 2025-12-26 15:51:16
*/
public interface SysRoleService extends IService<SysRole> {

    /**
     * 新增角色
     * @param roleDTO 角色信息
     * @return 结果
     */
    boolean saveRole(RoleDTO roleDTO);

    /**
     * 修改角色
     * @param roleDTO 角色信息
     * @return 结果
     */
    boolean updateRole(RoleDTO roleDTO);

    /**
     * 批量删除角色
     * @param roleIds 角色ID集合
     * @return 结果
     */
    boolean deleteRoles(List<Long> roleIds);

    /**
     * 根据ID获取角色详情
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleVO getRoleById(Long roleId);

    /**
     * 分页查询角色列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    ResultPage<RoleVO> pageRoles(RoleQueryDTO queryDTO);

    Boolean grantAll(String roleKey);

    List<SysMenu> getRolesMenus(List<Long> roleIds);
}
