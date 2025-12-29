package blog.yuanyuan.yuanlive.user.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FlagValidatorClass implements ConstraintValidator<FlagValidator, String> {
    private String[] values;

    @Override
    public void initialize(FlagValidator constraintAnnotation) {
        this.values = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true; // 当值为空时，允许通过
        }
        for (String val : values) {
            if (val.equals(value)) { // 检查当前值是否在允许的值集合中
                return true;
            }
        }
        return false; // 如果不在允许的值集合中，返回 false
    }
}
