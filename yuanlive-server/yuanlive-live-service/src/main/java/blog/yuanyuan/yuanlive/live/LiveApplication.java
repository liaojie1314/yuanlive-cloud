package blog.yuanyuan.yuanlive.live;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = {"blog.yuanyuan.yuanlive.feign"})
@ComponentScan(basePackages = {"blog.yuanyuan.yuanlive"})
@MapperScan("blog.yuanyuan.yuanlive.live.mapper")
public class LiveApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiveApplication.class, args);
    }
}
