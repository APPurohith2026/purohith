package com.purohitcommunity.portal.domain;
import com.purohitcommunity.portal.domain.Enums.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class MembershipPaymentInitializationTest {
 @Test void newPendingMembershipSnapshotsPriceAndCreatesTrackingId(){
  var plan=MembershipPlan.builder().price(new BigDecimal("2500")).build();
  var membership=Membership.builder().plan(plan).status(MembershipStatus.PENDING).build();
  membership.initializePayment();
  assertTrue(membership.getPaymentTrackingId().startsWith("PAY-"));assertEquals(40,membership.getPaymentTrackingId().length());
  assertEquals(PaymentStatus.PENDING,membership.getPaymentStatus());
  plan.setPrice(new BigDecimal("3000"));assertEquals(new BigDecimal("2500"),membership.getPaymentAmount());
 }
 @Test void adminCreatedActiveMembershipDoesNotRequirePayment(){
  var membership=Membership.builder().status(MembershipStatus.ACTIVE).build();membership.initializePayment();
  assertNull(membership.getPaymentTrackingId());
 }
}
