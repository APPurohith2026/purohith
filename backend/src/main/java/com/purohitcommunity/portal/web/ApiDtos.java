package com.purohitcommunity.portal.web;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.Set;
import com.purohitcommunity.portal.domain.Enums.*;

public final class ApiDtos {
  private ApiDtos(){}

  public record RegistrationRequest(
      @NotBlank @Size(max=140) String fullName,
      RegistrationSource registrationSource,
      @Size(max=40) String memberReferenceId,
      @NotBlank @Size(max=20) String gender,
      @NotBlank @Size(max=120) String profession,
      @NotBlank @Size(max=120) String gotram,
      @Size(max=220) String pravara,
      @NotNull LocalDate dateOfBirth,
      @NotBlank @Pattern(regexp="\\d{12}", message="Aadhaar number must contain 12 digits") String aadhaarNumber,
      @NotBlank @Size(max=10) String bloodGroup,
      @NotBlank @Pattern(regexp="[0-9+ -]{8,18}", message="Invalid mobile number") String phoneNumber,
      @NotBlank @Pattern(regexp="[0-9+ -]{8,18}", message="Invalid WhatsApp number") String whatsappNumber,
      @NotBlank @Size(max=80) String doorNumber,
      @NotBlank @Size(max=100) String cityTown,
      @NotBlank @Size(max=120) String constituency,
      @NotBlank @Size(max=100) String district,
      @NotBlank @Pattern(regexp="\\d{6}", message="PIN code must contain 6 digits") String pinCode,
      @NotBlank @Size(max=60) String state,
      @Size(min=8,max=72) String password,
      @NotNull PlanCode planCode,
      @AssertTrue(message="Declaration must be accepted") boolean declarationAccepted
  ) {}


  public record DraftStep1Request(
      @NotBlank @Size(max=140) String fullName,
      RegistrationSource registrationSource,
      @Size(max=40) String memberReferenceId,
      @NotBlank @Size(max=20) String gender,
      @NotBlank @Size(max=120) String profession,
      @NotBlank @Size(max=120) String gotram,
      @Size(max=220) String pravara,
      @NotNull LocalDate dateOfBirth,
      @NotBlank @Pattern(regexp="(?:\\d{12}|__KEEP__)", message="Aadhaar number must contain 12 digits") String aadhaarNumber,
      @NotBlank @Size(max=10) String bloodGroup
  ) {}
  public record DraftStep2Request(
      @NotBlank @Pattern(regexp="[0-9+ -]{8,18}", message="Invalid mobile number") String phoneNumber,
      @NotBlank @Pattern(regexp="[0-9+ -]{8,18}", message="Invalid WhatsApp number") String whatsappNumber,
      @NotBlank @Size(max=80) String doorNumber,
      @NotBlank @Size(max=100) String cityTown,
      @NotBlank @Size(max=120) String constituency,
      @NotBlank @Size(max=100) String district,
      @NotBlank @Pattern(regexp="\\d{6}", message="PIN code must contain 6 digits") String pinCode,
      @NotBlank @Size(max=60) String state
  ) {}
  public record DraftStep4Request(@NotNull PlanCode planCode) {}
  public record DraftConfirmRequest(
      @Size(min=8,max=72) String password,
      @AssertTrue(message="Declaration must be accepted") boolean declarationAccepted
  ) {}
  public record DraftView(
      String draftToken, Integer lastCompletedStep,
      String fullName, RegistrationSource registrationSource, String memberReferenceId, String gender, String profession, String gotram, String pravara,
      LocalDate dateOfBirth, String aadhaarMasked, String bloodGroup,
      String phoneNumber, String whatsappNumber, String doorNumber, String cityTown, String constituency,
      String district, String districtCode, String pinCode, String state,
      String aadhaarFrontName, String aadhaarBackName, String passportPhotoName, PlanCode planCode
  ) {}

  public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {}
  public record AuthResponse(String token, String role, String loginId, String memberNumber) {}
  public record ProfileUpdateRequest(@NotBlank String fullName, LocalDate dateOfBirth, String gender, String profession, String gotram, String pravara) {}
  public record EnrollmentRequest(@NotNull PlanCode planCode) {}
  public record PlanView(Long id, PlanCode code, String nameTelugu, String nameEnglish, BigDecimal price, Integer validityMonths) {}
  public record MemberView(
      Long id, String email, String memberNumber, String memberReferenceId,
      String fullNameEnglish, String fullNameTelugu,
      String phoneNumber, String whatsappNumber,
      LocalDate dateOfBirth, String gender, String profession, String gotram, String pravara, String bloodGroup,
      String aadhaarMasked, String doorNumber, String cityTown, String constituency, String district, String districtCode, String pinCode, String state,
      String passportPhotoPath, String aadhaarFrontPath, String aadhaarBackPath,
      ApplicationStatus applicationStatus, String reviewComment, RegistrationSource registrationSource,
      String referrerMemberNumber, String referrerName, PlanCode planCode, MembershipStatus membershipStatus, LocalDate expiryDate, Instant createdAt
  ) {}
  public record ActivityRequest(@NotBlank String titleTelugu, @NotBlank String titleEnglish, String descriptionTelugu, String descriptionEnglish, String venue, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime registrationDeadline, @Min(1) Integer capacity, @NotEmpty Set<PlanCode> allowedPlans, ActivityStatus status) {}
  public record ActivityView(Long id, String titleTelugu, String titleEnglish, String descriptionTelugu, String descriptionEnglish, String venue, LocalDateTime startDateTime, LocalDateTime endDateTime, LocalDateTime registrationDeadline, Integer capacity, Set<PlanCode> allowedPlans, ActivityStatus status, boolean eligible, boolean registered) {}
  public record ReviewRequest(String comment) {}
  public record IdCardView(String memberNumber, String nameEnglish, String nameTelugu, String district, PlanCode planCode, LocalDate issuedOn, LocalDate validUntil, String qrToken, String verificationUrl) {}
  public record DashboardView(MemberView member, long availableActivities, long registrations) {}
  public record AdminStats(
      long totalApplications, long pendingApplications, long approvedMembers, long correctionRequired,
      long rejectedApplications, long activeMembers, long premiumMembers, long eliteMembers,
      long directRegistrations, long referredRegistrations, long draftRegistrations,
      long publishedActivities, long totalActivityEnrollments
  ) {}
  public record AdminDashboard(AdminStats stats, java.util.List<MemberView> recentApplications) {}
  public record ReferenceValidation(boolean valid, String memberNumber, String memberName, String district, String message) {}
  public record FoundingMemberRequest(
      @NotBlank String fullName,
      @NotBlank @Pattern(regexp="[0-9+ -]{8,18}") String phoneNumber,
      @NotBlank String state, @NotBlank String district,
      @NotNull PlanCode planCode, @NotBlank @Size(min=8,max=72) String password
  ) {}
  public record ApplicationActionRequest(String comment) {}
}
