package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.user.domain.dto.PasswordDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.UserQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.UserRoleDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.UserDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.RouterVO;
import blog.yuanyuan.yuanlive.user.domain.vo.UserVO;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "管理端接口")
public class AdminController {

    @Resource
    SysUserService userService;

    @PostMapping
    @Operation(summary = "管理员新增用户")
    public Result<Boolean> add(@RequestBody @Validated(Insert.class) UserDTO userDTO) {
        return Result.success(userService.saveUser(userDTO));
    }

    @PutMapping
    @Operation(summary = "修改用户")
    public Result<Boolean> edit(@RequestBody @Validated(Update.class) UserDTO userDTO) {
        return Result.success(userService.updateUser(userDTO));
    }

    @DeleteMapping("/{userIds}")
    @Operation(summary = "批量删除用户")
    public Result<Boolean> remove(@PathVariable("userIds") List<Long> userIds) {
        return Result.success(userService.deleteOrRestoreUsers(userIds));
    }

    @GetMapping("/getInfo/{userId}")
    @Operation(summary = "获取用户详情")
    public Result<UserVO> getInfo(@PathVariable("userId") Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    @PostMapping("/list")
    @Operation(summary = "分页查询用户列表")
    public Result<ResultPage<UserVO>> list(@RequestBody UserQueryDTO queryDTO) {
        return Result.success(userService.pageUsers(queryDTO));
    }

    @PostMapping("/assignRoles")
    @Operation(summary = "为用户分配角色")
    public Result<Boolean> assignRoles(@RequestBody @Validated UserRoleDTO userRoleDTO) {
        return Result.success(userService.assignRoles(userRoleDTO));
    }

    @GetMapping("/getRouters")
    @Operation(summary = "获取管理端用户路由")
    public Result<List<RouterVO>> getRouters() {
        return Result.success(userService.getRouters());
    }

    @PutMapping("/password")
    @Operation(summary = "管理员修改用户密码")
    public Result<Boolean> changePassword(@RequestBody @Validated PasswordDTO passwordDTO) {
        return Result.success(userService.changePassword(passwordDTO));
    }

    @PutMapping("/updateStatus/{uid}")
    @Operation(summary = "修改用户状态")
    public Result<Boolean> updateStatus(@PathVariable("uid") Long uid) {
        return Result.success(userService.updateStatus(uid));
    }

    @PutMapping("/restore/{uid}")
    @Operation(summary = "恢复或删除用户")
    public Result<Boolean> restore(@PathVariable("uid") Long uid) {
        return Result.success(userService.restoreOrDelete(uid));
    }
}
