package blog.yuanyuan.yuanlive.common.exception;

import blog.yuanyuan.yuanlive.common.result.ResultCode;

public class ApiException extends RuntimeException {
    private ResultCode resultCode;

    public ApiException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.resultCode = resultCode;
    }
    public ApiException(String message) {
        super(message);
        this.resultCode = ResultCode.FAILED;
    }
    public ResultCode getResultCode() {
        return resultCode;
    }
}
