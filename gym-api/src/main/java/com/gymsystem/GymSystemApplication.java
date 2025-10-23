// gymsystem/GymSystemApplication
package com.gymsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Main entry-point for the Gym SaaS API.
 */
@EnableScheduling
@SpringBootApplication
public class GymSystemApplication {

    /**
     * Standard Java main method that launches the Spring Boot application.
     * @param args command-line arguments (unused here)
     */
    public static void main(String[] args) {
        SpringApplication.run(GymSystemApplication.class, args);
    }
}
