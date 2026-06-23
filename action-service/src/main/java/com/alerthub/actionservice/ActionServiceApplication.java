package com.alerthub.actionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ActionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActionServiceApplication.class, args);
    }
}