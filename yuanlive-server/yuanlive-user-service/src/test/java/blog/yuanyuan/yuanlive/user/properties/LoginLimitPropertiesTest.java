package blog.yuanyuan.yuanlive.user.properties;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {LoginLimitProperties.class})
@EnableConfigurationProperties(LoginLimitProperties.class) // 强制启用配置绑定
@TestPropertySource("classpath:application.yml")
class LoginLimitPropertiesTest {
    @Resource
    private LoginLimitProperties loginLimitProperties;

    @Test
    public void test() {
        LoginLimitProperties.LimitConfig lock = loginLimitProperties.getLock();
        LoginLimitProperties.LimitConfig failCount = loginLimitProperties.getFailCount();
        System.out.println("lock: " + lock.toString());
        System.out.println("fail-count: " + failCount.toString());
    }

}