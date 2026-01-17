package blog.yuanyuan.yuanlive.security;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object o, String s) {
        // 如果是 super-admin，直接赋予所有权限 (*)
        if (getRoles().contains("super-admin")) {
            return Collections.singletonList("*");
        }
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        List<String> list = getRoles();
        return !CollectionUtils.isEmpty(list) ? list : Collections.emptyList();
    }

    private static List<String> getRoles() {
        String role = StpUtil.getSession().getString("role");
        return Arrays.stream(role.split(",")).toList();
    }
}
