package com.example.sameCity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j //可以直接使用log变量
@SpringBootApplication
@ServletComponentScan  //使其扫描webfilter注解
@EnableTransactionManagement
@EnableCaching
public class sameCityApplication {
    public static void main(String[] args) {
        SpringApplication.run(sameCityApplication.class,args);
        log.info("项目启动成功。。。");
    }
}
