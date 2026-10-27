package com.grocery.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GroceryPosApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroceryPosApplication.class, args);
    }
}
