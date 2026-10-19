package com.purohitcommunity.portal.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import static com.purohitcommunity.portal.domain.Enums.PlanCode;

@Entity @Table(name="membership_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MembershipPlan {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable=false, unique=true, length=20)
    private PlanCode code;
    @Column(nullable=false, length=80)
    private String nameTelugu;
    @Column(nullable=false, length=80)
    private String nameEnglish;
    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal price;
    @Column(nullable=false)
    private Integer validityMonths;
    @Column(nullable=false)
    private boolean active;
}
