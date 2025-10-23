// src/main/java/com/gymsystem/booking/BookingStatus.java
package com.gymsystem.booking;

/**
 * Enumerates the possible states of a booking.
 */
public enum BookingStatus { // Declares the BookingStatus enum
    BOOKED, // The booking is active and counts towards capacity
    CANCELED // The booking was canceled and should not count towards capacity
}
