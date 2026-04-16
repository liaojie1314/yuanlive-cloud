package blog.yuanyuan.yuanlive.entity.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 */
@Schema(description = "性别枚举")
@Getter
@AllArgsConstructor
public enum GenderEnum {
    UNKNOWN(0, "未知"),
    MALE(1, "男性"),
    FEMALE(2, "女性");

    @EnumValue
    private final Integer value;
    private final String description;

//    GenderEnum(Integer value, String description) {
//        this.value = value;
//        this.description = description;
//    }

//    @JsonValue
//    public Integer getValue() {
//        return value;
//    }

    public static GenderEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (GenderEnum gender : values()) {
            if (gender.value.equals(value)) {
                return gender;
            }
        }
        return null;
    }
}