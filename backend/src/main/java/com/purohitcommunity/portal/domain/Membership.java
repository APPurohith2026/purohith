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
    @Column(unique=true, length=40)
    private String paymentTrackingId;
    @Column(unique=true, length=64)
    private String paymentReference;
    @Enumerated(EnumType.STRING) @Column(length=20)
    private com.purohitcommunity.portal.domain.Enums.PaymentStatus paymentStatus;
    @Column(precision=10, scale=2)
    private java.math.BigDecimal paymentAmount;
    private Instant paymentSubmittedAt;
    private Instant paymentVerifiedAt;

    @PrePersist void initializePayment(){
        if(status==MembershipStatus.PENDING && paymentTrackingId==null){
            paymentTrackingId="PAY-"+java.util.UUID.randomUUID();
            paymentStatus=com.purohitcommunity.portal.domain.Enums.PaymentStatus.PENDING;
            paymentAmount=plan.getPrice();
        }
    }
}
