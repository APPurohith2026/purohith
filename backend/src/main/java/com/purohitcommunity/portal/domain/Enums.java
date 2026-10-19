package com.purohitcommunity.portal.domain;

public final class Enums {
    private Enums() {}
    public enum Role { MEMBER, ADMIN }
    public enum ApplicationStatus { DRAFT, PENDING_REVIEW, CORRECTION_REQUIRED, APPROVED, REJECTED }
    public enum MembershipStatus { PENDING, ACTIVE, EXPIRED, SUSPENDED }
    public enum PlanCode { PREMIUM, ELITE }
    public enum ActivityStatus { DRAFT, PUBLISHED, CLOSED, CANCELLED }
    public enum RegistrationStatus { REGISTERED, CANCELLED }
    public enum PaymentStatus { PENDING, VERIFIED, FAILED, REFUNDED, MANUAL_REVIEW }
    public enum RegistrationSource { DIRECT, MEMBER_REFERENCE, ADMIN_CREATED, IMPORTED }
}
