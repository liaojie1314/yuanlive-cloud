package blog.yuanyuan.yuanlive.common.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@Component
public class IdCardUtil {
    public boolean isValidIdCard(String idCard) {
        if (idCard == null || !Pattern.matches("^\\d{17}[\\dxX]$", idCard)) {
            return false;
        }

        idCard = idCard.toUpperCase();
        String birthdayStr = idCard.substring(6, 14); // 获取第7-14位
        if (!isValidDate(birthdayStr)) {
            return false;
        }

        return checkCode(idCard);
    }

    /**
     * 校验日期是否合法
     */
    private boolean isValidDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate birthDate = LocalDate.parse(dateStr, formatter);
            // 不能早于1900年，不能晚于当前日期
            return birthDate.isBefore(LocalDate.now()) && birthDate.isAfter(LocalDate.of(1900, 1, 1));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 计算并校验最后一位校验码
     */
    private boolean checkCode(String idCard) {
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * weights[i];
        }

        int index = sum % 11;
        char targetCheckCode = checkCodes[index];

        return idCard.charAt(17) == targetCheckCode;
    }
}
