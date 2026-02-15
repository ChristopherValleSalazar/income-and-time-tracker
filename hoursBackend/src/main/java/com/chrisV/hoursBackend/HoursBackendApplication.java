package com.chrisV.hoursBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@SpringBootApplication
public class HoursBackendApplication {

	public static void main(String[] args) {
        SpringApplication.run(HoursBackendApplication.class, args);
        System.out.println("Hours Backend Application is running...");
    }
}
