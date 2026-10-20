package com.purohitcommunity.portal.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import static com.purohitcommunity.portal.domain.Enums.Role;

@Entity
@Table(
        name = "users",
        indexes = @Index(name="idx_users_password_reset_challenge", columnList="password_reset_challenge"),
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 160, nullable = true)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(length = 36) private String passwordResetChallenge;
    private String passwordResetOtpHash;
    private String passwordResetTokenHash;
    private Instant passwordResetExpiresAt;
    private Instant passwordResetLastSentAt;
    private Instant passwordResetWindowStart;
    @Column(nullable=false, columnDefinition="integer default 0") private int passwordResetSendCount;
    @Column(nullable=false, columnDefinition="integer default 0") private int passwordResetAttempts;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void created() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
