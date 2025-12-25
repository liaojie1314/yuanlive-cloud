package blog.yuanyuan.yuanlive.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
@Tag(name = "HelloController", description = "hello")
public class HelloController {

    @GetMapping("/world")
    @Operation(summary = "hello world")
    public String helloWorld() {
        return "Hello World";
    }
}
