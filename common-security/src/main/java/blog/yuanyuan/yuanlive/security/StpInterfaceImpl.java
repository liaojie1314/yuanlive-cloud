package blog.yuanyuan.yuanlive.security;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object o, String s) {
        List<String> perms = getValues("perms");
        // 如果是 super-admin，直接赋予所有权限 (*)
        if (getValues("role").contains("super-admin")) {
            perms.add("*");
        }
        return perms;
    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        List<String> list = getValues("role");
        return !CollectionUtils.isEmpty(list) ? list : Collections.emptyList();
    }

    private static List<String> getValues(String key) {
        String value = StpUtil.getSession().getString(key);
        if (StrUtil.isBlank(value)) return new ArrayList<>();
        String[] arr = value.split(",");
        return new ArrayList<>(Arrays.asList(arr));
    }
}
