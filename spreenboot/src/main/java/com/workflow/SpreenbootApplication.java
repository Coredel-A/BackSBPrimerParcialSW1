package com.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.workflow") 
public class SpreenbootApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpreenbootApplication.class, args);
    }
}
