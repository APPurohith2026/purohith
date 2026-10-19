package com.purohitcommunity.portal.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import static com.purohitcommunity.portal.domain.Enums.MembershipStatus;

@Entity @Table(name="memberships")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Membership {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private MemberProfile member;
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    private MembershipPlan plan;
    private LocalDate startDate;
    private LocalDate expiryDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private MembershipStatus status;
    private Instant approvedAt;
}
