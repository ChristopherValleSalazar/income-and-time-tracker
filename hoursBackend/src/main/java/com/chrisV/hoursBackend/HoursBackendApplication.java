package com.chrisV.hoursBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HoursBackendApplication {

	public static void main(String[] args) {
        SpringApplication.run(HoursBackendApplication.class, args);
        System.out.println("Hours Backend Application is running...");
    }
}
