package com.purohitcommunity.portal.service;

import com.purohitcommunity.portal.domain.*;
import com.purohitcommunity.portal.domain.Enums.*;
import com.purohitcommunity.portal.repository.*;
import com.purohitcommunity.portal.web.ApiDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class PortalService {
  private final UserAccountRepository users;
  private final MemberProfileRepository members;
  private final MembershipPlanRepository plans;
  private final MembershipRepository memberships;
  private final ActivityRepository activities;
  private final ActivityRegistrationRepository registrations;
  private final DigitalIdCardRepository cards;
  private final RegistrationDraftRepository drafts;
  private final PasswordEncoder encoder;
  private final MemberIdService memberIdService;

  @Transactional(readOnly=true) public MemberProfile current(String principal){
    UserAccount u = users.findByPhoneNumber(principal).orElseGet(() -> users.findByEmailIgnoreCase(principal).orElseThrow());
    return members.findByUserId(u.getId()).orElseThrow();
  }

  @Transactional(readOnly=true) public MemberView memberView(MemberProfile m){
    var ms=memberships.findFirstByMemberIdOrderByIdDesc(m.getId()).orElse(null);
    var ref=m.getReferrerMember();
    return new MemberView(
        m.getId(), m.getUser().getEmail(), m.getMemberNumber(), m.getMemberReferenceId(),
        m.getFullNameEnglish(), m.getFullNameTelugu(),
        m.getAlternateContactNumber(), m.getWhatsappNumber(),
        m.getDateOfBirth(), m.getGender(), m.getProfession(), m.getGotram(), m.getPravara(), m.getBloodGroup(),
        maskAadhaar(m.getAadhaarNumber()), m.getDoorNumber(), m.getCityTown(), m.getConstituency(), m.getDistrict(), m.getDistrictCode(), m.getPinCode(), m.getState(),
        m.getPassportPhotoPath(), m.getAadhaarFrontPath(), m.getAadhaarBackPath(),
        m.getApplicationStatus(), m.getReviewComment(), m.getRegistrationSource(),
        ref==null?null:ref.getMemberNumber(), ref==null?null:(ref.getFullNameTelugu()!=null?ref.getFullNameTelugu():ref.getFullNameEnglish()),
        ms==null?null:ms.getPlan().getCode(), ms==null?null:ms.getStatus(), ms==null?null:ms.getExpiryDate(), m.getCreatedAt());
  }

  @Transactional public MemberView updateProfile(String email, ProfileUpdateRequest r){
    var m=current(email);
    m.setFullNameTelugu(r.fullName()); m.setFullNameEnglish(r.fullName());
    if(r.dateOfBirth()!=null) m.setDateOfBirth(r.dateOfBirth());
    if(r.gender()!=null) m.setGender(r.gender());
    if(r.profession()!=null) m.setProfession(r.profession());
    if(r.gotram()!=null) m.setGotram(r.gotram());
    if(r.pravara()!=null) m.setPravara(r.pravara());
    return memberView(members.save(m));
  }

  @Transactional public MemberView enroll(String email, EnrollmentRequest r){
    var m=current(email); var p=plans.findByCode(r.planCode()).orElseThrow();
    var existing=memberships.findFirstByMemberIdOrderByIdDesc(m.getId()).orElse(null);
    if(!p.isActive()) throw new IllegalArgumentException("This plan is not available");
    if(existing!=null && existing.getStatus()==MembershipStatus.ACTIVE) throw new IllegalStateException("You already have an active membership");
    if(existing!=null && existing.getStatus()==MembershipStatus.PENDING){
      if(existing.getPlan().getCode()!=p.getCode()) throw new IllegalStateException("Contact admin to change your selected plan before paying");
      return memberView(m);
    }
    memberships.save(Membership.builder().member(m).plan(p).status(MembershipStatus.PENDING).build());
    m.setApplicationStatus(ApplicationStatus.PENDING_REVIEW); m.setReviewComment(null); return memberView(members.save(m));
  }

  @Transactional(readOnly=true) public List<ActivityView> memberActivities(String email){
    var m=current(email); var ms=memberships.findFirstByMemberIdOrderByIdDesc(m.getId()).orElse(null); PlanCode code=ms==null?null:ms.getPlan().getCode();
    return activities.findByStatusOrderByStartDateTimeAsc(ActivityStatus.PUBLISHED).stream().map(a->view(a,m,code)).toList();
  }
  private ActivityView view(Activity a, MemberProfile m, PlanCode code){
    boolean eligible=code!=null && a.getAllowedPlans().contains(code) && m.getApplicationStatus()==ApplicationStatus.APPROVED;
    boolean registered=m!=null && registrations.existsByActivityIdAndMemberId(a.getId(),m.getId());
    return new ActivityView(a.getId(),a.getTitleTelugu(),a.getTitleEnglish(),a.getDescriptionTelugu(),a.getDescriptionEnglish(),a.getVenue(),a.getStartDateTime(),a.getEndDateTime(),a.getRegistrationDeadline(),a.getCapacity(),a.getAllowedPlans(),a.getStatus(),eligible,registered);
  }
  private ActivityView adminActivityView(Activity a){
    return new ActivityView(a.getId(),a.getTitleTelugu(),a.getTitleEnglish(),a.getDescriptionTelugu(),a.getDescriptionEnglish(),a.getVenue(),a.getStartDateTime(),a.getEndDateTime(),a.getRegistrationDeadline(),a.getCapacity(),a.getAllowedPlans(),a.getStatus(),false,false);
  }

  @Transactional public void registerActivity(String email, Long id){
    var m=current(email); var a=activities.findById(id).orElseThrow(); var ms=memberships.findFirstByMemberIdOrderByIdDesc(m.getId()).orElseThrow();
    if(m.getApplicationStatus()!=ApplicationStatus.APPROVED || ms.getStatus()!=MembershipStatus.ACTIVE || !a.getAllowedPlans().contains(ms.getPlan().getCode())) throw new IllegalStateException("Activity is not available for this membership");
    if(a.getRegistrationDeadline()!=null && LocalDateTime.now().isAfter(a.getRegistrationDeadline())) throw new IllegalStateException("Registration closed");
    if(a.getCapacity()!=null && registrations.countByActivityId(a.getId())>=a.getCapacity()) throw new IllegalStateException("Activity is full");
    if(!registrations.existsByActivityIdAndMemberId(a.getId(),m.getId())) registrations.save(ActivityRegistration.builder().activity(a).member(m).status(RegistrationStatus.REGISTERED).build());
  }

  @Transactional(readOnly=true) public DashboardView dashboard(String email){ var m=current(email); var list=memberActivities(email); return new DashboardView(memberView(m),list.stream().filter(ActivityView::eligible).count(),registrations.findByMemberIdOrderByRegisteredAtDesc(m.getId()).size()); }
  @Transactional(readOnly=true) public IdCardView card(String email, String baseUrl){ var m=current(email); var c=cards.findByMemberId(m.getId()).orElseThrow(); var ms=memberships.findFirstByMemberIdOrderByIdDesc(m.getId()).orElseThrow(); return new IdCardView(m.getMemberNumber(),m.getFullNameEnglish(),m.getFullNameTelugu(),m.getDistrict(),ms.getPlan().getCode(),c.getIssuedOn(),c.getValidUntil(),c.getQrToken(),baseUrl+"/verify/"+c.getQrToken()); }

  @Transactional public MemberView review(Long memberId, ApplicationStatus status, String comment){
    var m=members.findById(memberId).orElseThrow(); m.setApplicationStatus(status); m.setReviewComment(comment);
    var ms=memberships.findFirstByMemberIdOrderByIdDesc(memberId).orElse(null);
    if(status==ApplicationStatus.APPROVED){
      if(ms==null) throw new IllegalStateException("No membership plan selected");
      if(ms.getPaymentTrackingId()!=null && ms.getPaymentStatus()!=PaymentStatus.VERIFIED)
        throw new IllegalStateException("Verify the membership payment before approval");
      LocalDate start=LocalDate.now(); ms.setStartDate(start); ms.setExpiryDate(start.plusMonths(ms.getPlan().getValidityMonths())); ms.setStatus(MembershipStatus.ACTIVE); ms.setApprovedAt(java.time.Instant.now());
      cards.findByMemberId(memberId).orElseGet(()->cards.save(DigitalIdCard.builder().member(m).qrToken(UUID.randomUUID().toString()).issuedOn(start).validUntil(ms.getExpiryDate()).active(true).build()));
    } else if(ms!=null && status==ApplicationStatus.REJECTED){
      ms.setStatus(MembershipStatus.SUSPENDED);
    }
    return memberView(members.save(m));
  }

  @Transactional(readOnly=true) public List<MemberView> allMembers(){ return members.findAll().stream().sorted(Comparator.comparing(MemberProfile::getCreatedAt).reversed()).map(this::memberView).toList(); }
  @Transactional(readOnly=true) public MemberView member(Long id){ return memberView(members.findById(id).orElseThrow()); }
  @Transactional(readOnly=true) public List<MemberView> applications(ApplicationStatus status){
    var source=status==null?members.findAll():members.findByApplicationStatusOrderByCreatedAtAsc(status);
    return source.stream().filter(m->m.getApplicationStatus()!=ApplicationStatus.DRAFT).sorted(Comparator.comparing(MemberProfile::getCreatedAt).reversed()).map(this::memberView).toList();
  }

  @Transactional(readOnly=true) public AdminStats stats(){
    return new AdminStats(
      members.count(),
      members.countByApplicationStatus(ApplicationStatus.PENDING_REVIEW),
      members.countByApplicationStatus(ApplicationStatus.APPROVED),
      members.countByApplicationStatus(ApplicationStatus.CORRECTION_REQUIRED),
      members.countByApplicationStatus(ApplicationStatus.REJECTED),
      memberships.countByStatus(MembershipStatus.ACTIVE),
      memberships.countByPlanAndStatus(PlanCode.PREMIUM,MembershipStatus.ACTIVE),
      memberships.countByPlanAndStatus(PlanCode.ELITE,MembershipStatus.ACTIVE),
      members.countByRegistrationSource(RegistrationSource.DIRECT),
      members.countByRegistrationSource(RegistrationSource.MEMBER_REFERENCE),
      drafts.countBySubmittedFalse(),
      activities.findByStatusOrderByStartDateTimeAsc(ActivityStatus.PUBLISHED).size(),
      registrations.count()
    );
  }

  @Transactional(readOnly=true) public AdminDashboard adminDashboard(){
    var recent=allMembers().stream().limit(8).toList();
    return new AdminDashboard(stats(),recent);
  }

  @Transactional(readOnly=true) public ReferenceValidation validateReference(String memberNumber){
    if(memberNumber==null || memberNumber.isBlank()) return new ReferenceValidation(false,null,null,null,"Member ID is required");
    var m=members.findByMemberNumberIgnoreCase(memberNumber.trim()).orElse(null);
    if(m==null || m.getApplicationStatus()!=ApplicationStatus.APPROVED) return new ReferenceValidation(false,null,null,null,"Approved member not found");
    var ms=memberships.findFirstByMemberIdOrderByIdDesc(m.getId()).orElse(null);
    if(ms==null || ms.getStatus()!=MembershipStatus.ACTIVE) return new ReferenceValidation(false,null,null,null,"Reference member is not active");
    return new ReferenceValidation(true,m.getMemberNumber(),m.getFullNameTelugu()!=null?m.getFullNameTelugu():m.getFullNameEnglish(),m.getDistrict(),"Valid active member");
  }

  @Transactional public MemberView createFoundingMember(FoundingMemberRequest r){
    String phone=r.phoneNumber().replaceAll("\\D","");
    if(phone.length()<10 || phone.length()>15) throw new IllegalArgumentException("Invalid phone number");
    if(users.existsByPhoneNumber(phone)) throw new IllegalArgumentException("Phone number already registered");
    var plan=plans.findByCode(r.planCode()).orElseThrow();
    String password=r.password();
    var u=users.save(UserAccount.builder().phoneNumber(phone).passwordHash(encoder.encode(password)).role(Role.MEMBER).enabled(true).build());
    var m=members.save(MemberProfile.builder().user(u).fullNameTelugu(r.fullName()).fullNameEnglish(r.fullName())
      .alternateContactNumber(phone).state(r.state()).district(r.district()).districtCode(memberIdService.districtCode(r.district()))
      .applicationStatus(ApplicationStatus.APPROVED).registrationSource(RegistrationSource.ADMIN_CREATED).declarationAccepted(true).build());
    m.setMemberNumber(memberIdService.generate(m.getState(),m.getDistrict(),m.getId())); m=members.save(m);
    LocalDate start=LocalDate.now();
    var ms=memberships.save(Membership.builder().member(m).plan(plan).status(MembershipStatus.ACTIVE).startDate(start).expiryDate(start.plusMonths(plan.getValidityMonths())).approvedAt(java.time.Instant.now()).build());
    cards.save(DigitalIdCard.builder().member(m).qrToken(UUID.randomUUID().toString()).issuedOn(start).validUntil(ms.getExpiryDate()).active(true).build());
    return memberView(m);
  }

  @Transactional(readOnly=true) public List<ActivityView> adminActivities(){ return activities.findAll().stream().sorted(Comparator.comparing(Activity::getId).reversed()).map(this::adminActivityView).toList(); }
  @Transactional public ActivityView saveActivity(Long id, ActivityRequest r){
    Activity a=id==null?new Activity():activities.findById(id).orElseThrow(); a.setTitleTelugu(r.titleTelugu()); a.setTitleEnglish(r.titleEnglish()); a.setDescriptionTelugu(r.descriptionTelugu()); a.setDescriptionEnglish(r.descriptionEnglish()); a.setVenue(r.venue()); a.setStartDateTime(r.startDateTime()); a.setEndDateTime(r.endDateTime()); a.setRegistrationDeadline(r.registrationDeadline()); a.setCapacity(r.capacity()); a.setAllowedPlans(new HashSet<>(r.allowedPlans())); a.setStatus(r.status()==null?ActivityStatus.DRAFT:r.status()); a=activities.save(a); return adminActivityView(a);
  }
  @Transactional public void deleteMember(Long id){
    var member=members.findById(id).orElseThrow();
    var user=member.getUser();
    if(user.getRole()!=Role.MEMBER) throw new IllegalArgumentException("Admin accounts cannot be deleted here");
    for(var referred:members.findByReferrerMemberId(id)){
      referred.setReferrerMember(null);
      referred.setMemberReferenceId(null);
    }
    registrations.deleteAll(registrations.findByMemberIdOrderByRegisteredAtDesc(id));
    cards.findByMemberId(id).ifPresent(cards::delete);
    memberships.deleteByMemberId(id);
    if(user.getPhoneNumber()!=null) drafts.deleteByPhoneNumber(user.getPhoneNumber());
    members.flush();
    members.delete(member);
    members.flush();
    users.delete(user);
  }
  @Transactional public void deleteActivity(Long id){ activities.deleteById(id); }
  @Transactional(readOnly=true) public List<ActivityView> publicActivities(){ return activities.findByStatusOrderByStartDateTimeAsc(ActivityStatus.PUBLISHED).stream().map(this::adminActivityView).toList(); }
  @Transactional(readOnly=true) public IdCardView verify(String token, String baseUrl){ var c=cards.findByQrTokenAndActiveTrue(token).orElseThrow(); var m=c.getMember(); var ms=memberships.findFirstByMemberIdOrderByIdDesc(m.getId()).orElseThrow(); return new IdCardView(m.getMemberNumber(),m.getFullNameEnglish(),m.getFullNameTelugu(),m.getDistrict(),ms.getPlan().getCode(),c.getIssuedOn(),c.getValidUntil(),c.getQrToken(),baseUrl+"/verify/"+token); }
  private String maskAadhaar(String aadhaar){ if(aadhaar==null || aadhaar.length()<4) return null; return "XXXX XXXX " + aadhaar.substring(aadhaar.length()-4); }
}
