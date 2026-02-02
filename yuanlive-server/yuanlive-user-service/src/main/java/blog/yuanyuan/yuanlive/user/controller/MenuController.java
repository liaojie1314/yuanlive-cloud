package blog.yuanyuan.yuanlive.user.controller;

import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.entity.user.entity.SysMenu;
import blog.yuanyuan.yuanlive.user.domain.dto.MenuDTO;
import blog.yuanyuan.yuanlive.user.domain.vo.MenuVO;
import blog.yuanyuan.yuanlive.user.service.SysMenuService;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Update;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/menu")
@Tag(name = "权限接口")
public class MenuController {

    @Resource
    private SysMenuService sysMenuService;

    @GetMapping("/tree")
    @Operation(summary = "获取菜单树")
    public Result<List<MenuVO>> tree() {
        return Result.success(sysMenuService.treeList());
    }

    @PostMapping("/add")
    @Operation(summary = "新增菜单")
    public Result<Boolean> add(@RequestBody @Validated MenuDTO menuDTO) {
        return Result.success(sysMenuService.addMenu(menuDTO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改菜单")
    public Result<Boolean> update(@RequestBody @Validated(Update.class) MenuDTO menuDTO) {
        return Result.success(sysMenuService.updateMenu(menuDTO));
    }

    @DeleteMapping("/delete/{menuId}")
    @Operation(summary = "删除菜单")
    public Result<Boolean> delete(@PathVariable("menuId") Long menuId) {
        return Result.success(sysMenuService.removeMenuById(menuId));
    }

    @GetMapping("/getInfo/{menuId}")
    @Operation(summary = "获取菜单详情")
    public Result<MenuVO> getInfo(@PathVariable("menuId") Long menuId) {
        SysMenu menu = sysMenuService.getById(menuId);
        return Result.success(BeanUtil.copyProperties(menu, MenuVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获取菜单列表")
    public Result<List<MenuVO>> list() {
        List<SysMenu> list = sysMenuService.list();
        List<MenuVO> voList = list.stream()
                .map(menu -> BeanUtil.copyProperties(menu, MenuVO.class))
                .collect(Collectors.toList());
        return Result.success(voList);
    }
}
