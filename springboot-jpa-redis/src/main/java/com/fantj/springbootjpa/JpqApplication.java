package com.fantj.springbootjpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.fantj.springbootjpa.pojo")
public class JpqApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpqApplication.class, args);
    }
}
