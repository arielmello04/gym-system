// src/main/java/com/gymsystem/documents/UserDocument.java
package com.gymsystem.documents;

import com.gymsystem.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/** Stores file metadata and where it lives in the local storage. */
@Entity
@Table(name = "user_documents")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDocument {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The owner of this document. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** Short human-readable label for listings. */
    @Column(nullable = false, length = 100)
    private String title;

    /** Simple category to help coaches filter documents. */
    @Column(nullable = false, length = 20)
    private String category; // DIET | MEDICAL | OTHER

    /** Persisted for content-disposition when downloading. */
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    /** Stored once to display approximate size on UI. */
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    /** Relative path on the configured storage base path. */
    @Column(name = "storage_path", nullable = false, length = 255)
    private String storagePath;

    /** Audit fields. */
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedBy;
}
