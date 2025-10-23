// src/main/java/com/gymsystem/common/CommonConfig.java 
package com.gymsystem.common;

import org.springframework.context.annotation.Bean; // Bean annotation
import org.springframework.context.annotation.Configuration; // Configuration annotation
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // BCrypt encoder

/**
 * Common configuration beans used across the application.
 */
@Configuration
public class CommonConfig {

    /**
     * Provides a BCryptPasswordEncoder bean for hashing passwords.
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Returns new BCrypt encoder
    }
}
