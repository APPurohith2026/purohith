package com.purohitcommunity.portal.web;

import com.purohitcommunity.portal.domain.*;
import com.purohitcommunity.portal.domain.Enums.*;
import com.purohitcommunity.portal.repository.*;
import com.purohitcommunity.portal.security.CustomUserDetailsService;
import com.purohitcommunity.portal.security.JwtService;
import com.purohitcommunity.portal.service.MemberIdService;
import com.purohitcommunity.portal.storage.PrivateFileStorageService;
import com.purohitcommunity.portal.web.ApiDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {
  private final RegistrationDraftRepository drafts;
  private final com.purohitcommunity.portal.service.DraftLoginService draftLogin;
  private final UserAccountRepository users;
  private final MemberProfileRepository members;
  private final MembershipPlanRepository plans;
  private final MembershipRepository memberships;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final CustomUserDetailsService userDetails;
  private final PrivateFileStorageService storage;
  private final MemberIdService memberIdService;
  private final com.purohitcommunity.portal.service.RegistrationValidationService validation;

  @PostMapping("/drafts/start")
  @Transactional
  public DraftView start(@Valid @RequestBody DraftStep1Request r){
    if("__KEEP__".equals(r.aadhaarNumber())) throw new IllegalArgumentException("Aadhaar number is required for a new draft");
    var d=RegistrationDraft.builder()
        .draftToken(UUID.randomUUID().toString().replace("-", ""))
        .lastCompletedStep(0)
        .submitted(false)
        .build();
    applyStep1(d,r);
    d.setLastCompletedStep(1);
    return view(drafts.save(d));
  }

  @PostMapping("/drafts/resume")
  public DraftView resume(@Valid @RequestBody LoginRequest r){ return view(draftLogin.authenticate(r.identifier(),r.password())); }

  @GetMapping("/drafts/{token}")
  @Transactional(readOnly=true)
  public DraftView get(@PathVariable String token){ return view(find(token)); }

  @PutMapping("/drafts/{token}/step/1")
  @Transactional
  public DraftView step1(@PathVariable String token,@Valid @RequestBody DraftStep1Request r){
    var d=find(token); applyStep1(d,r); complete(d,1); return view(drafts.save(d));
  }

  @PutMapping("/drafts/{token}/step/2")
  @Transactional
  public DraftView step2(@PathVariable String token,@Valid @RequestBody DraftStep2Request r){
    var d=find(token);
    String phone=validation.phone(r.phoneNumber());
    validation.phone(r.whatsappNumber());
    validation.location(r.state(),r.district());
    if(users.existsByPhoneNumber(phone)) throw new IllegalArgumentException("Mobile number already registered");
    draftLogin.configure(d,phone,r.password());
    d.setPhoneNumber(phone); d.setWhatsappNumber(validation.phone(r.whatsappNumber())); d.setDoorNumber(r.doorNumber().trim());
    d.setCityTown(r.cityTown().trim()); d.setConstituency(r.constituency().trim()); d.setDistrict(r.district().trim());
    d.setDistrictCode(memberIdService.districtCode(r.district())); d.setPinCode(r.pinCode()); d.setState(r.state());
    complete(d,2); return view(drafts.save(d));
  }

  @PutMapping(value="/drafts/{token}/step/3", consumes="multipart/form-data")
  @Transactional
  public DraftView step3(@PathVariable String token,
      @RequestPart(value="aadhaarFront",required=false) MultipartFile aadhaarFront,
      @RequestPart(value="aadhaarBack",required=false) MultipartFile aadhaarBack,
      @RequestPart(value="passportPhoto",required=false) MultipartFile passportPhoto){
    var d=find(token);
    if(d.getLastCompletedStep()<2) throw new IllegalStateException("Complete address details before uploading documents");
    if(aadhaarFront!=null && !aadhaarFront.isEmpty()) d.setAadhaarFrontPath(storage.storeDocument(aadhaarFront,"draft-aadhaar-front"));
    if(aadhaarBack!=null && !aadhaarBack.isEmpty()) d.setAadhaarBackPath(storage.storeDocument(aadhaarBack,"draft-aadhaar-back"));
    if(passportPhoto!=null && !passportPhoto.isEmpty()) d.setPassportPhotoPath(storage.storeImage(passportPhoto,"draft-passport-photo"));
    if(d.getAadhaarFrontPath()==null || d.getAadhaarBackPath()==null || d.getPassportPhotoPath()==null)
      throw new IllegalArgumentException("Aadhaar front, Aadhaar back and passport photo are required");
    complete(d,3); return view(drafts.save(d));
  }

  @PutMapping("/drafts/{token}/step/4")
  @Transactional
  public DraftView step4(@PathVariable String token,@Valid @RequestBody DraftStep4Request r){
    var d=find(token);
    if(d.getLastCompletedStep()<3) throw new IllegalStateException("Complete document uploads before choosing a plan");
    plans.findByCode(r.planCode()).filter(p->p.isActive()).orElseThrow(() -> new IllegalArgumentException("Selected membership plan is not available"));
    d.setPlanCode(r.planCode()); complete(d,4); return view(drafts.save(d));
  }

  @PostMapping("/drafts/{token}/confirm")
  @Transactional
  public AuthResponse confirm(@PathVariable String token,@Valid @RequestBody DraftConfirmRequest r){
    var d=find(token);
    if(d.getLastCompletedStep()<4) throw new IllegalStateException("Complete all registration steps before submission");
    validation.validateDraft(d);
    if(d.getPhoneNumber()==null) throw new IllegalStateException("Mobile number is missing");
    if(users.existsByPhoneNumber(d.getPhoneNumber())) throw new IllegalArgumentException("Mobile number already registered");
    if(d.getAadhaarFrontPath()==null || d.getAadhaarBackPath()==null || d.getPassportPhotoPath()==null) throw new IllegalStateException("Required documents are missing");
    if(d.getPlanCode()==null) throw new IllegalStateException("Membership plan is missing");

    var plan=plans.findByCode(d.getPlanCode()).filter(p->p.isActive()).orElseThrow(() -> new IllegalArgumentException("Selected membership plan is not available"));
    var u=users.save(UserAccount.builder().phoneNumber(d.getPhoneNumber()).passwordHash(draftLogin.passwordHashForSubmission(d,r.password())).role(Role.MEMBER).enabled(true).build());
    var m=members.save(MemberProfile.builder()
        .user(u).memberReferenceId(d.getMemberReferenceId()).registrationSource(d.getRegistrationSource()).referrerMember(d.getRegistrationSource()==RegistrationSource.MEMBER_REFERENCE?validReferrer(d.getMemberReferenceId()):null).fullNameTelugu(d.getFullName()).fullNameEnglish(d.getFullName())
        .dateOfBirth(d.getDateOfBirth()).gender(d.getGender()).profession(d.getProfession()).gotram(d.getGotram()).pravara(d.getPravara())
        .bloodGroup(d.getBloodGroup()).aadhaarNumber(d.getAadhaarNumber()).alternateContactNumber(d.getPhoneNumber()).whatsappNumber(d.getWhatsappNumber())
        .doorNumber(d.getDoorNumber()).cityTown(d.getCityTown()).constituency(d.getConstituency()).district(d.getDistrict()).districtCode(d.getDistrictCode())
        .pinCode(d.getPinCode()).state(d.getState()).passportPhotoPath(d.getPassportPhotoPath()).aadhaarFrontPath(d.getAadhaarFrontPath()).aadhaarBackPath(d.getAadhaarBackPath())
        .declarationAccepted(r.declarationAccepted()).applicationStatus(ApplicationStatus.PENDING_REVIEW).build());
    m.setMemberNumber(memberIdService.generate(m.getState(),m.getDistrict(),m.getId())); m=members.save(m);
    memberships.save(Membership.builder().member(m).plan(plan).status(MembershipStatus.PENDING).build());
    d.setSubmitted(true); d.setDraftLoginPhone(null); d.setPasswordHash(null); d.setLastCompletedStep(5); drafts.save(d);
    String loginId=userDetails.canonicalLogin(u);
    return new AuthResponse(jwt.generate(loginId,u.getRole().name()),u.getRole().name(),loginId,m.getMemberNumber());
  }

  @GetMapping("/reference/{memberNumber}")
  @Transactional(readOnly=true)
  public ReferenceValidation reference(@PathVariable String memberNumber){
    var ref=validReferrer(memberNumber);
    return new ReferenceValidation(true,ref.getMemberNumber(),ref.getFullNameTelugu()!=null?ref.getFullNameTelugu():ref.getFullNameEnglish(),ref.getDistrict(),"Valid active member");
  }

  private RegistrationDraft find(String token){ return drafts.findByDraftTokenAndSubmittedFalse(token).orElseThrow(() -> new IllegalArgumentException("Registration draft not found or already submitted")); }
  private void complete(RegistrationDraft d,int step){ d.setLastCompletedStep(Math.max(d.getLastCompletedStep()==null?0:d.getLastCompletedStep(),step)); }
  private void applyStep1(RegistrationDraft d,DraftStep1Request r){
    validation.personal(r.registrationSource(),r.memberReferenceId());
    RegistrationSource source=r.registrationSource()==null?RegistrationSource.DIRECT:r.registrationSource();
    String reference=r.memberReferenceId()==null?null:r.memberReferenceId().trim().toUpperCase();
    if(source==RegistrationSource.MEMBER_REFERENCE){
      if(reference==null || reference.isBlank()) throw new IllegalArgumentException("Member Reference ID is required for referred registration");
      validReferrer(reference);
    } else reference=null;
    d.setRegistrationSource(source); d.setFullName(r.fullName().trim()); d.setMemberReferenceId(reference); d.setGender(r.gender()); d.setProfession(r.profession().trim());
    d.setGotram(r.gotram().trim()); d.setPravara(r.pravara()==null?null:r.pravara().trim()); d.setDateOfBirth(r.dateOfBirth()); if(!"__KEEP__".equals(r.aadhaarNumber())) d.setAadhaarNumber(r.aadhaarNumber()); d.setBloodGroup(r.bloodGroup());
  }
  private MemberProfile validReferrer(String memberNumber){
    var ref=members.findByMemberNumberIgnoreCase(memberNumber==null?"":memberNumber.trim()).orElseThrow(() -> new IllegalArgumentException("Approved reference member not found"));
    if(ref.getApplicationStatus()!=ApplicationStatus.APPROVED) throw new IllegalArgumentException("Reference member is not approved");
    var ms=memberships.findFirstByMemberIdOrderByIdDesc(ref.getId()).orElseThrow(() -> new IllegalArgumentException("Reference member has no active membership"));
    if(ms.getStatus()!=MembershipStatus.ACTIVE) throw new IllegalArgumentException("Reference member is not active");
    return ref;
  }
  private DraftView view(RegistrationDraft d){
    return new DraftView(d.getDraftToken(),d.getLastCompletedStep(),d.getFullName(),d.getRegistrationSource(),d.getMemberReferenceId(),d.getGender(),d.getProfession(),d.getGotram(),d.getPravara(),d.getDateOfBirth(),mask(d.getAadhaarNumber()),d.getBloodGroup(),
        d.getPhoneNumber(),d.getWhatsappNumber(),d.getDoorNumber(),d.getCityTown(),d.getConstituency(),d.getDistrict(),d.getDistrictCode(),d.getPinCode(),d.getState(),
        fileName(d.getAadhaarFrontPath()),fileName(d.getAadhaarBackPath()),fileName(d.getPassportPhotoPath()),d.getPlanCode(),d.getPasswordHash()!=null);
  }
  private String mask(String value){ return value==null||value.length()<4?null:"XXXX XXXX "+value.substring(value.length()-4); }
  private String fileName(String value){ if(value==null||value.isBlank()) return null; try{return Path.of(value).getFileName().toString();}catch(Exception e){return "Uploaded";} }
}
