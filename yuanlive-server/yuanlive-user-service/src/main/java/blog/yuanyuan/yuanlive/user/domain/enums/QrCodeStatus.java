package blog.yuanyuan.yuanlive.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QrCodeStatus {
    WAITING(0, "等待扫码"),
    SCANNED(1, "已扫码"),
    CONFIRMED(2, "已确认"),
//    CANCELED(3, "已取消"),
    TIMEOUT(4, "已过期");

    private final int status;
    private final String desc;

    public static QrCodeStatus getByStatus(int status) {
        for (QrCodeStatus value : values()) {
            if (value.status == status) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + status);
    }

}
