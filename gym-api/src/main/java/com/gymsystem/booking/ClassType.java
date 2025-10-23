// src/main/java/com/gymsystem/booking/ClassType.java
package com.gymsystem.booking;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a type of class (e.g., Pilates, Strength, Massage).
 */
@Entity
@Table(name = "class_types")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Uses identity strategy for auto-increment
    private Long id; // Unique identifier for the class type

    @Column(nullable = false, unique = true, length = 64)
    private String code; // Machine-friendly unique code (e.g., "PILATES")

    @Column(nullable = false, length = 128)
    private String name; // Human-friendly display name (e.g., "Pilates")

    @Column
    private String description; // Optional extended description

    @Column(nullable = false)
    private boolean active; // Whether this class type is currently active
}
