package result;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "未登录"),
    TOKEN_EXPIRED(406, "Token已过期"),
    FORBIDDEN(403, "用户无权限访问"),
    FAILED(500, "操作失败");


    private final Integer code;
    private final String msg;
}
