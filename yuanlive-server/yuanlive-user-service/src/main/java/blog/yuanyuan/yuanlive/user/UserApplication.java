package blog.yuanyuan.yuanlive.user;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("blog.yuanyuan.yuanlive.user.mapper")
@ComponentScan(basePackages = {"blog.yuanyuan.yuanlive.user", "blog.yuanyuan.yuanlive.common"})
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
