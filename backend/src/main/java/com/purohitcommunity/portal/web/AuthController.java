package com.purohitcommunity.portal.web;

import com.purohitcommunity.portal.domain.*;
import com.purohitcommunity.portal.domain.Enums.*;
import com.purohitcommunity.portal.repository.*;
import com.purohitcommunity.portal.security.JwtService;
import com.purohitcommunity.portal.security.CustomUserDetailsService;
import com.purohitcommunity.portal.service.MemberIdService;
import com.purohitcommunity.portal.storage.PrivateFileStorageService;
import com.purohitcommunity.portal.web.ApiDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
  private final UserAccountRepository users;
  private final MemberProfileRepository members;
  private final MembershipPlanRepository plans;
  private final MembershipRepository memberships;
  private final PasswordEncoder encoder;
  private final AuthenticationManager auth;
  private final JwtService jwt;
  private final CustomUserDetailsService userDetails;
  private final PrivateFileStorageService storage;
  private final MemberIdService memberIdService;
  private final com.purohitcommunity.portal.service.RegistrationValidationService validation;

  @PostMapping(value="/register", consumes="multipart/form-data")
  @Transactional
  public AuthResponse register(
      @Valid @RequestPart("data") RegistrationRequest r,
      @RequestPart("aadhaarFront") MultipartFile aadhaarFront,
      @RequestPart("aadhaarBack") MultipartFile aadhaarBack,
      @RequestPart("passportPhoto") MultipartFile passportPhoto
  ) {
    validation.personal(r.registrationSource(),r.memberReferenceId());
    validation.location(r.state(),r.district());
    validation.phone(r.whatsappNumber());
    String phone = validation.phone(r.phoneNumber());
    if(phone.length() < 10 || phone.length() > 15) throw new IllegalArgumentException("Invalid mobile number");
    if(users.existsByPhoneNumber(phone)) throw new IllegalArgumentException("Mobile number already registered");

    String aadhaar = r.aadhaarNumber().replaceAll("\\D", "");
    var plan = plans.findByCode(r.planCode()).filter(p->p.isActive()).orElseThrow(() -> new IllegalArgumentException("Selected membership plan is not available"));

    String frontPath = storage.storeDocument(aadhaarFront, "aadhaar-front");
    String backPath = storage.storeDocument(aadhaarBack, "aadhaar-back");
    String photoPath = storage.storeImage(passportPhoto, "passport-photo");

    var u = users.save(UserAccount.builder()
        .phoneNumber(phone)
        .passwordHash(encoder.encode(r.password()))
        .role(Role.MEMBER)
        .enabled(true)
        .build());

    RegistrationSource source = r.registrationSource()==null?RegistrationSource.DIRECT:r.registrationSource();
    MemberProfile referrer = null; String reference = null;
    if(source==RegistrationSource.MEMBER_REFERENCE){
      reference=r.memberReferenceId()==null?null:r.memberReferenceId().trim().toUpperCase();
      if(reference==null || reference.isBlank()) throw new IllegalArgumentException("Member Reference ID is required");
      referrer=members.findByMemberNumberIgnoreCase(reference).filter(x->x.getApplicationStatus()==ApplicationStatus.APPROVED).orElseThrow(() -> new IllegalArgumentException("Approved reference member not found"));
      var rms=memberships.findFirstByMemberIdOrderByIdDesc(referrer.getId()).orElseThrow(() -> new IllegalArgumentException("Reference member has no active membership"));
      if(rms.getStatus()!=MembershipStatus.ACTIVE) throw new IllegalArgumentException("Reference member is not active");
    }
    var m = members.save(MemberProfile.builder()
        .user(u)
        .registrationSource(source)
        .referrerMember(referrer)
        .memberReferenceId(reference)
        .fullNameTelugu(r.fullName().trim())
        .fullNameEnglish(r.fullName().trim())
        .dateOfBirth(r.dateOfBirth())
        .gender(r.gender())
        .profession(r.profession().trim())
        .gotram(r.gotram().trim())
        .pravara(r.pravara()==null?null:r.pravara().trim())
        .bloodGroup(r.bloodGroup())
        .aadhaarNumber(aadhaar)
        .alternateContactNumber(phone)
        .whatsappNumber(r.whatsappNumber().trim())
        .doorNumber(r.doorNumber().trim())
        .cityTown(r.cityTown().trim())
        .constituency(r.constituency().trim())
        .district(r.district().trim())
        .districtCode(memberIdService.districtCode(r.district()))
        .pinCode(r.pinCode())
        .state(r.state())
        .passportPhotoPath(photoPath)
        .aadhaarFrontPath(frontPath)
        .aadhaarBackPath(backPath)
        .declarationAccepted(r.declarationAccepted())
        .applicationStatus(ApplicationStatus.PENDING_REVIEW)
        .build());

    // Generate the permanent member number immediately after the database-generated numeric ID exists.
    // Format: <STATE>-<DISTRICT>-<GENERATED_ID>, e.g. TS-HYD-000125.
    m.setMemberNumber(memberIdService.generate(m.getState(), m.getDistrict(), m.getId()));
    m = members.save(m);

    memberships.save(Membership.builder()
        .member(m)
        .plan(plan)
        .status(MembershipStatus.PENDING)
        .build());

    String loginId = userDetails.canonicalLogin(u);
    return new AuthResponse(jwt.generate(loginId,u.getRole().name()),u.getRole().name(),loginId,m.getMemberNumber());
  }

  @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest r){
    auth.authenticate(new UsernamePasswordAuthenticationToken(r.identifier(),r.password()));
    var u=userDetails.resolve(r.identifier());
    String memberNumber=members.findByUserId(u.getId()).map(MemberProfile::getMemberNumber).orElse(null);
    String loginId=userDetails.canonicalLogin(u);
    return new AuthResponse(jwt.generate(loginId,u.getRole().name()),u.getRole().name(),loginId,memberNumber);
  }
}
