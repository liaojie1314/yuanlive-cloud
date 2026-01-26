package blog.yuanyuan.yuanlive.common.handler;

import blog.yuanyuan.yuanlive.common.exception.ApiException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import blog.yuanyuan.yuanlive.common.result.Result;
import blog.yuanyuan.yuanlive.common.result.ResultCode;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Hidden
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleException(SQLIntegrityConstraintViolationException e) {
        String message = e.getMessage();
        String name = message.split(" ")[2];
        Result<String> result = Result.failed(name + "已存在");
        log.warn("Response     : {}", JSONUtil.toJsonStr(result));
        return result;
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        Result<String> result = Result.failed(message);
        log.warn("Response     : {}", JSONUtil.toJsonStr(result));
        return result;
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                // 2. 提取错误信息 (只取 message，不取 field，这样更干净)
                .map(ObjectError::getDefaultMessage)
                // 3. 用分号或换行符连接
                .collect(Collectors.joining("\n"));
        Result<String> result = Result.failed(ResultCode.VALIDATE_FAILED, message);
        log.warn("Response     : {}", JSONUtil.toJsonStr(result));
        return result;
    }

    @ExceptionHandler(ApiException.class)
    public Result<String> handleException(ApiException e) {
        Result<String> result = Result.failed(e.getResultCode());
        result.setMsg(e.getMessage());
        log.warn("Response     : {}", JSONUtil.toJsonStr(result));
        return result;
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<String> handleException(NotRoleException e) {
        Result<String> result = Result.failed(ResultCode.FORBIDDEN);
        log.warn("Response     : {}", JSONUtil.toJsonStr(result));
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        Result<String> result = Result.failed(e.getMessage());
        log.warn("Response     : {}", JSONUtil.toJsonStr(result));
        return result;
    }
}
