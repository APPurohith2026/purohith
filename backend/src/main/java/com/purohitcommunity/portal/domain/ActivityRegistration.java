package com.purohitcommunity.portal.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import static com.purohitcommunity.portal.domain.Enums.RegistrationStatus;

@Entity @Table(name="activity_registrations", uniqueConstraints=@UniqueConstraint(columnNames={"activity_id","member_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityRegistration {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="activity_id")
    private Activity activity;
    @ManyToOne(optional=false) @JoinColumn(name="member_id")
    private MemberProfile member;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private RegistrationStatus status;
    @Column(nullable=false, updatable=false)
    private Instant registeredAt;
    @PrePersist void created(){ if(registeredAt==null) registeredAt=Instant.now(); }
}
