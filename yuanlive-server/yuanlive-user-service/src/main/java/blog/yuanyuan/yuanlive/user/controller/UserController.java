package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultPage;
import blog.yuanyuan.yuanlive.user.domain.dto.UserQueryDTO;
import blog.yuanyuan.yuanlive.user.domain.dto.UserRoleDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.UserVO;
import blog.yuanyuan.yuanlive.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "user-controller", description = "用户管理")
public class UserController {

    @Resource
    SysUserService userService;

//    @PostMapping
//    @Operation(summary = "新增用户")
//    public Result<Boolean> add(@RequestBody @Validated UserDTO userDTO) {
//        return Result.success(userService.saveUser(userDTO));
//    }
//
//    @PutMapping
//    @Operation(summary = "修改用户")
//    public Result<Boolean> edit(@RequestBody @Validated(Update.class) UserDTO userDTO) {
//        return Result.success(userService.updateUser(userDTO));
//    }
//
//    @DeleteMapping("/{userIds}")
//    @Operation(summary = "批量删除用户")
//    public Result<Boolean> remove(@PathVariable List<Long> userIds) {
//        return Result.success(userService.deleteUsers(userIds));
//    }
//
    @GetMapping("/getInfo/{userId}")
    @Operation(summary = "获取用户详情")
    public Result<UserVO> getInfo(@PathVariable("userId") Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表")
    public Result<ResultPage<UserVO>> list(@ParameterObject UserQueryDTO queryDTO) {
        return Result.success(userService.pageUsers(queryDTO));
    }

    @PostMapping("/assignRoles")
    @Operation(summary = "为用户分配角色")
    public Result<Boolean> assignRoles(@RequestBody @Validated UserRoleDTO userRoleDTO) {
        return Result.success(userService.assignRoles(userRoleDTO));
    }

}
