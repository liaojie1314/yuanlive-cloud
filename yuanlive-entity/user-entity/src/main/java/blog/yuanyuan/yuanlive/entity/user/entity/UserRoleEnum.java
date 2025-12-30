package blog.yuanyuan.yuanlive.entity.user.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRoleEnum {
    USER(0, "普通用户"),
    ANCHOR(1, "主播"),
    ADMIN(2, "管理员");

    @EnumValue()
    private final int code;
    private final String desc;
}
