package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.user.domain.dto.RoleDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.RoleQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.RoleVO;
import blog.yuanyuan.yuanlive.user.service.SysRoleService;
import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Update;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@Tag(name = "role-controller", description = "角色管理")
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

    @DeleteMapping("/delete/{roleIds}")
    @Operation(summary = "批量删除角色")
    public Result<String> remove(@PathVariable("roleIds") List<Long> roleIds) {
        if (roleService.deleteRoles(roleIds)) {
            return Result.success(null, "删除成功");
        }
        return Result.failed("删除失败");
    }

    @GetMapping("/getInfo/{roleId}")
    @Operation(summary = "获取角色详情")
    public Result<RoleVO> getInfo(@PathVariable("roleId") Long roleId) {
        return Result.success(roleService.getRoleById(roleId));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询角色列表")
    public Result<ResultPage<RoleVO>> list(@ParameterObject RoleQueryDTO queryDTO) {
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
}
