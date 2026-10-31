package com.shiguang.book;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.shiguang.book.mapper")
public class ShiguangApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShiguangApplication.class, args);
    }
}
