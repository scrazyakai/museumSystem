package com.design.museum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.design.museum.mapper")
public class MuseumApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuseumApplication.class, args);
    }

}
