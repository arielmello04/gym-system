// src/main/java/com/gymsystem/user/User.java
package com.gymsystem.user;

import jakarta.persistence.*; // Imports JPA annotations
import jakarta.validation.constraints.Email; // Imports Email validation
import jakarta.validation.constraints.NotBlank; // Imports NotBlank validation
import lombok.*; // Imports Lombok annotations for boilerplate
import java.time.Instant; // Imports Instant for timestamps

/**
 * Represents an application user (member or admin).
 */
@Entity // Marks this class as a JPA entity
@Table(name = "users", uniqueConstraints = { // Maps to the "users" table and defines unique constraints
        @UniqueConstraint(name = "uk_users_email", columnNames = "email") // Ensures email uniqueness
})
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id // Marks the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Uses auto-increment identity
    private Long id; // Database identifier

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email; // User's email (also username)

    @NotBlank
    @Column(name = "password_hash", nullable = false) // Column to store hashed password
    private String passwordHash; // Hashed password (BCrypt)

    @Enumerated(EnumType.STRING) // Stores enum as a string
    @Column(nullable = false)
    private UserRole role; // Role determining permissions

    @Column(nullable = false)
    private boolean active; // Indicates if the account is active

    @Column(name = "created_at", nullable = false, updatable = false) // Creation timestamp
    private Instant createdAt; // When the user was created

    @Column(name = "updated_at", nullable = false) // Update timestamp
    private Instant updatedAt; // When the user was last updated
}
