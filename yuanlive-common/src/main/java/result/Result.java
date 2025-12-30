package result;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    // 默认接口版本号
    private static final String DEFAULT_VERSION = "1.0";

    private T data;
    private Integer code;
    private String msg;
    private boolean success;
    // 接口版本
    private String version;
    private Result(Integer code, String msg, T data, boolean success) {
        this(code, msg, data, DEFAULT_VERSION, success);
    }

    private Result(Integer code, String msg, T data, String version, boolean success) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.version = version;
        this.success = success;
    }
    /**
     * 操作成功
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null, true);
    }
    /**
     * 操作成功
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data, true);
    }

    /**
     * 操作成功
     * 自定义消息
     */
    public static <T> Result<T> success(T data, String msg) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data, true);
    }

    /**
     * 操作失败
     */
    public static <T> Result<T> failed() {
        return new Result<>(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMsg(), null, false);
    }

    /**
     * 操作失败
     * 自定义信息
     * @param msg
     * @return
     * @param <T>
     */
    public static <T> Result<T> failed(String msg) {
        return new Result<>(ResultCode.FAILED.getCode(), msg, null, false);
    }
    /**
     * 操作失败
     * 使用枚举
     * @param resultCode
     * @param <T>
     */
    public static <T> Result<T> failed(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null, false);
    }
    public static <T> Result<T> failed(ResultCode resultCode, String msg) {
        return new Result<>(resultCode.getCode(), msg, null, false);
    }



}
