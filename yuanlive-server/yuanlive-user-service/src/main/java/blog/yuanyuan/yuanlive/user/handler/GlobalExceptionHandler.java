package blog.yuanyuan.yuanlive.user.handler;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import result.Result;
import result.ResultCode;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleException(SQLIntegrityConstraintViolationException e) {
        String message = e.getMessage();
        String name = message.split(" ")[2];
        return Result.failed(name + "已存在");
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return Result.failed(message);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                // 2. 提取错误信息 (只取 message，不取 field，这样更干净)
                .map(ObjectError::getDefaultMessage)
                // 3. 用分号或换行符连接
                .collect(Collectors.joining("\n"));
        return Result.failed(ResultCode.VALIDATE_FAILED, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        return Result.failed(e.getMessage());
    }
}
