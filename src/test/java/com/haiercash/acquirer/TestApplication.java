package com.haiercash.acquirer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by Administrator on 2017/3/14.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"test.com.haiercash.acquirer.controller", "com.haiercash.acquirer.service",
        "com.haiercash.acquirer", "com.haiercash.cmis", "com.haiercash.commons"})
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
