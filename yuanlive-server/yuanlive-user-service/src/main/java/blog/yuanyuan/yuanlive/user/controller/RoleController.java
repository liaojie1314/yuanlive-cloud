package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.entity.user.entity.SysRole;
import blog.yuanyuan.yuanlive.user.domain.dto.RoleDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RoleQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.RoleVO;
import blog.yuanyuan.yuanlive.user.service.SysRoleService;
import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Update;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@Tag(name = "角色管理接口")
public class RoleController {
    @Resource
    SysRoleService roleService;

    @PostMapping("/add")
    @Operation(summary = "新增角色")
    public Result<String> add(@RequestBody @Validated RoleDTO roleDTO) {
        if (roleService.saveRole(roleDTO)) {
            return Result.success(null, "新增成功");
        }
        return Result.failed("新增失败");
    }

    @PostMapping("/update")
    @Operation(summary = "修改角色")
    public Result<String> update(@RequestBody @Validated(Update.class) RoleDTO roleDTO) {
        if (roleService.updateRole(roleDTO)) {
            return Result.success(null, "修改成功");
        }
        return Result.failed("修改失败");
    }

    @PostMapping("/assignRoleMenus")
    @Operation(summary = "分配角色权限")
    public Result<String> assignRoleMenus(@RequestBody @Validated(Update.class) RoleDTO roleDTO) {
        if (roleService.assignRoleMenus(roleDTO)) {
            return Result.success(null, "分配成功");
        }
        return Result.failed("分配失败");
    }
    @DeleteMapping("/delete/{roleId}")
    @Operation(summary = "删除角色")
    public Result<String> remove(@PathVariable("roleId") Long roleId) {
        if (roleService.deleteRole(roleId)) {
            return Result.success(null, "删除成功");
        }
        return Result.failed("删除失败");
    }

    @GetMapping("/getInfo/{roleId}")
    @Operation(summary = "获取角色详情")
    public Result<RoleVO> getInfo(@PathVariable("roleId") Long roleId) {
        return Result.success(roleService.getRoleById(roleId));
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询角色列表")
    public Result<ResultPage<RoleVO>> list(@RequestBody RoleQueryDTO queryDTO) {
        return Result.success(roleService.pageRoles(queryDTO));
    }

    @PostMapping("/grantAll")
    @Operation(summary = "为指定角色分配所有权限 需要super-admin角色")
    @SaCheckRole("super-admin")
    public Result<String> grantAll(@RequestParam("roleKey") String roleKey) {
        if (roleService.grantAll(roleKey)) {
            return Result.success(null, "分配成功");
        }
        return Result.failed("分配失败,权限菜单为空");
    }

    @GetMapping("/getRoles")
    @Operation(summary = "获取所有角色")
    public Result<List<SysRole>> getRoles() {
        return Result.success(roleService.list());
    }

    @GetMapping("/getRoleIdsByUserId/{uid}")
    @Operation(summary = "根据userId，获取对应角色id列表")
    public Result<List<Long>> getRoleIdsByUserId(@PathVariable("uid") Long uid) {
        return Result.success(roleService.getRoleIdsByUserId(uid));
    }

    @PutMapping("/switchStatus/{roleId}")
    @Operation(summary = "切换角色状态")
    public Result<String> switchStatus(@PathVariable("roleId") Long roleId) {
        if (roleService.switchStatus(roleId)) {
            return Result.success(null, "切换成功");
        }
        return Result.failed("切换失败");
    }
}
