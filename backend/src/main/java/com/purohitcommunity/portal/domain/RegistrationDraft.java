package com.purohitcommunity.portal.domain;

import com.purohitcommunity.portal.domain.Enums.PlanCode;
import com.purohitcommunity.portal.domain.Enums.RegistrationSource;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name="registration_drafts", uniqueConstraints=@UniqueConstraint(columnNames="draft_token"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegistrationDraft {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="draft_token", nullable=false, length=64)
    private String draftToken;

    @Column(nullable=false)
    private Integer lastCompletedStep;

    @Column(nullable=false)
    private boolean submitted;

    @Column(length=140) private String fullName;
    @Column(length=40) private String memberReferenceId;
    @Enumerated(EnumType.STRING) @Column(length=30) private RegistrationSource registrationSource;
    @Column(length=20) private String gender;
    @Column(length=120) private String profession;
    @Column(length=120) private String gotram;
    @Column(length=220) private String pravara;
    private LocalDate dateOfBirth;
    @Column(length=12) private String aadhaarNumber;
    @Column(length=10) private String bloodGroup;

    @Column(length=100) private String passwordHash;
    @Column(unique=true,length=20) private String draftLoginPhone;
    private Integer loginFailures;
    private Instant loginLockedUntil;
    @Column(length=20) private String phoneNumber;
    @Column(length=20) private String whatsappNumber;
    @Column(length=80) private String doorNumber;
    @Column(length=100) private String cityTown;
    @Column(length=120) private String constituency;
    @Column(length=100) private String district;
    @Column(length=10) private String districtCode;
    @Column(length=6) private String pinCode;
    @Column(length=60) private String state;

    @Column(length=500) private String passportPhotoPath;
    @Column(length=500) private String aadhaarFrontPath;
    @Column(length=500) private String aadhaarBackPath;

    @Enumerated(EnumType.STRING) @Column(length=20)
    private PlanCode planCode;

    @Column(nullable=false, updatable=false)
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist void created(){
        if(lastCompletedStep==null) lastCompletedStep=0;
        if(registrationSource==null) registrationSource=RegistrationSource.DIRECT;
        createdAt=Instant.now(); updatedAt=createdAt;
    }
    @PreUpdate void updated(){ updatedAt=Instant.now(); }
}
