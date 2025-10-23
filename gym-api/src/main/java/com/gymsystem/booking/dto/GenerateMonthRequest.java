// src/main/java/com/gymsystem/booking/dto/GenerateMonthRequest.java
package com.gymsystem.booking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/** Admin payload to generate a month worth of sessions from business hours. */
@Data
public class GenerateMonthRequest {
    @Min(2000) @Max(2100)
    private int year;          // e.g., 2025

    @Min(1) @Max(12)
    private int month;         // 1..12

    @NotBlank
    private String classTypeCode; // target class type code

    @Min(15) @Max(480)
    private int slotMinutes;   // length of each session

    @Min(1) @Max(1000)
    private int capacity;      // capacity per session
}
