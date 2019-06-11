package com.n1ne.mmall;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAutoConfiguration
public class MmallCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MmallCustomerApplication.class, args);
    }

}
