package blog.yuanyuan.yuanlive.user.service.impl;

import jakarta.annotation.Resource;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceImplTest {
    @Resource
    private IdGeneratorProvider idGeneratorProvider;

    @Test
    void testCosID() {
        System.out.println(idGeneratorProvider.getRequired("user_id").generate());
    }
}