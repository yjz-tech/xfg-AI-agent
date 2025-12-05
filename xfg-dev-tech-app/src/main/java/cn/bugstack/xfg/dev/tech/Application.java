package cn.bugstack.xfg.dev.tech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

/**
 * Application下的都会被spring扫描到
 *
 * 启动类
 */
@SpringBootApplication
@Configuration
public class Application {
    public static void main(String[] args) {

        SpringApplication.run(Application.class);
    }

}
