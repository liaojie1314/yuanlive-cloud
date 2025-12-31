package blog.yuanyuan.yuanlive.common.result;

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
    VALIDATE_FAILED(404, "参数验证失败"),
    QRCODE_EXPIRE(400, "二维码已过期"),
    FAILED(500, "操作失败");


    private final Integer code;
    private final String msg;
}
