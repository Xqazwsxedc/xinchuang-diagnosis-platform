package com.xinchuang.diagnosis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.xinchuang.diagnosis.**.mapper")
@SpringBootApplication
public class DiagnosisPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiagnosisPlatformApplication.class, args);
    }
}

