// src/main/java/com/gymsystem/auth/invite/SignupToken.java
package com.gymsystem.auth.invite;

import com.gymsystem.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "signup_tokens")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SignupToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Admin who created the token (for audit). */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /** The token string itself (shared with prospective user). */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Optional expiration; if null, token never expires. */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /** How many times the token can be used (1 = single use). */
    @Column(name = "max_uses", nullable = false)
    private int maxUses;

    /** How many times it was actually used. */
    @Column(name = "used_count", nullable = false)
    private int usedCount;

    /** Soft-disable without deleting. */
    @Column(nullable = false)
    private boolean active;
}
