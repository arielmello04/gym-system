// src/main/java/com/gymsystem/checkin/CheckinStatus.java
package com.gymsystem.checkin;

/** Lifecycle states for a check-in record. */
public enum CheckinStatus {
    STARTED,   // Created, waiting provider callback (Gympass/TotalPass)
    COMPLETED,   // Confirmed by provider or direct check-in
    FAILED       // Provider rejected or unexpected error
}
