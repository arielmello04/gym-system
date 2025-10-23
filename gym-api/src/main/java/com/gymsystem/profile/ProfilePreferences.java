// src/main/java/com/gymsystem/profile/ProfilePreferences.java
package com.gymsystem.profile;

import com.gymsystem.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "profile_preferences")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfilePreferences {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "allow_recording", nullable = false)
    private boolean allowRecording;

    @Column(name = "allow_photos", nullable = false)
    private boolean allowPhotos;

    @Column(name = "allow_face_visibility", nullable = false)
    private boolean allowFaceVisibility;

    @Column(length = 500)
    private String notes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
