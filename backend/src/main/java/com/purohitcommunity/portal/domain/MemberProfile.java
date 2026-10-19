package com.purohitcommunity.portal.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import static com.purohitcommunity.portal.domain.Enums.ApplicationStatus;
import static com.purohitcommunity.portal.domain.Enums.RegistrationSource;

@Entity @Table(name="members", uniqueConstraints={@UniqueConstraint(columnNames="member_number"), @UniqueConstraint(columnNames="user_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberProfile {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="user_id")
    private UserAccount user;
    @Column(name="member_number", length=40)
    private String memberNumber;
    @Column(name="member_reference_id", length=40)
    private String memberReferenceId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="referrer_member_id")
    private MemberProfile referrerMember;
    @Enumerated(EnumType.STRING) @Column(name="registration_source", nullable=false, length=30)
    private RegistrationSource registrationSource;
    @Column(length=180)
    private String fullNameTelugu;
    @Column(length=140)
    private String fullNameEnglish;
    private LocalDate dateOfBirth;
    @Column(length=20)
    private String gender;
    @Column(length=120)
    private String profession;
    @Column(length=120)
    private String gotram;
    @Column(length=220)
    private String pravara;
    @Column(length=10)
    private String bloodGroup;
    @Column(length=12)
    private String aadhaarNumber;
    @Column(length=20)
    private String alternateContactNumber;
    @Column(length=20)
    private String whatsappNumber;
    @Column(length=80)
    private String doorNumber;
    @Column(length=100)
    private String cityTown;
    @Column(length=120)
    private String constituency;
    @Column(length=100)
    private String district;
    @Column(length=10)
    private String districtCode;
    @Column(length=6)
    private String pinCode;
    @Column(length=60)
    private String state;
    @Column(length=500)
    private String passportPhotoPath;
    @Column(length=500)
    private String aadhaarFrontPath;
    @Column(length=500)
    private String aadhaarBackPath;
    @Column(nullable=false)
    private boolean declarationAccepted;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30)
    private ApplicationStatus applicationStatus;
    @Column(length=500)
    private String reviewComment;
    @Column(nullable=false, updatable=false)
    private Instant createdAt;
    private Instant updatedAt;
    @PrePersist void created(){ createdAt=Instant.now(); updatedAt=createdAt; if(applicationStatus==null) applicationStatus=ApplicationStatus.DRAFT; if(registrationSource==null) registrationSource=RegistrationSource.DIRECT; }
    @PreUpdate void updated(){ updatedAt=Instant.now(); }
}
