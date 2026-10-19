package com.purohitcommunity.portal.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;
import static com.purohitcommunity.portal.domain.Enums.*;

@Entity @Table(name="activities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Activity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=180)
    private String titleTelugu;
    @Column(nullable=false, length=180)
    private String titleEnglish;
    @Column(length=2000)
    private String descriptionTelugu;
    @Column(length=2000)
    private String descriptionEnglish;
    @Column(length=250)
    private String venue;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime registrationDeadline;
    private Integer capacity;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="activity_membership_access", joinColumns=@JoinColumn(name="activity_id"))
    @Enumerated(EnumType.STRING) @Column(name="plan_code", length=20)
    @Builder.Default
    private Set<PlanCode> allowedPlans = new HashSet<>();
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private ActivityStatus status;
}
