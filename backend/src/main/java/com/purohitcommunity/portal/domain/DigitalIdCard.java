package com.purohitcommunity.portal.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="id_cards", uniqueConstraints={@UniqueConstraint(columnNames="member_id"), @UniqueConstraint(columnNames="qr_token")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DigitalIdCard {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional=false) @JoinColumn(name="member_id", nullable=false)
    private MemberProfile member;
    @Column(name="qr_token", nullable=false, unique=true, length=80)
    private String qrToken;
    @Column(nullable=false)
    private LocalDate issuedOn;
    private LocalDate validUntil;
    @Column(nullable=false)
    private boolean active;
}
