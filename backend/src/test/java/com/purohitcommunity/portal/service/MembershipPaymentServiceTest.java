package com.purohitcommunity.portal.service;
import com.purohitcommunity.portal.domain.*;
import com.purohitcommunity.portal.domain.Enums.*;
import com.purohitcommunity.portal.repository.MembershipRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class MembershipPaymentServiceTest {
 @Mock MembershipRepository memberships;
 @InjectMocks MembershipPaymentService payments;
 Membership membership;
 @BeforeEach void setup(){
  membership=Membership.builder().id(2L).plan(MembershipPlan.builder().code(PlanCode.PREMIUM).price(new BigDecimal("1000")).build())
   .paymentTrackingId("PAY-test").paymentAmount(new BigDecimal("1000")).paymentStatus(PaymentStatus.PENDING).build();
  when(memberships.findTopByMemberIdOrderByIdDesc(1L)).thenReturn(Optional.of(membership));
 }
 @Test void submissionNeedsAdminVerification(){
  var result=payments.submit(1L," abc123456 ");
  assertEquals("ABC123456",result.reference());assertEquals(PaymentStatus.MANUAL_REVIEW,result.status());
  assertNull(result.verifiedAt());assertEquals(new BigDecimal("1000"),result.amount());
  assertEquals(PaymentStatus.VERIFIED,payments.review(1L,true,membership.getPaymentReference()).status());assertNotNull(membership.getPaymentVerifiedAt());
 }
 @Test void staleReferenceCannotBeVerified(){
  payments.submit(1L,"123456789012");
  assertThrows(IllegalStateException.class,()->payments.review(1L,true,"123456789011"));
  assertEquals(PaymentStatus.MANUAL_REVIEW,membership.getPaymentStatus());
 }
 @Test void duplicateReferenceCannotBeReused(){
  when(memberships.existsByPaymentReferenceAndIdNot("123456789012",2L)).thenReturn(true);
  assertThrows(IllegalArgumentException.class,()->payments.submit(1L,"123456789012"));
  verify(memberships,never()).saveAndFlush(any());
 }
 @Test void invalidReferenceAndUnsubmittedVerificationAreRejected(){
  assertThrows(IllegalArgumentException.class,()->payments.submit(1L,""));
  assertThrows(IllegalArgumentException.class,()->payments.submit(1L,"<script>"));
  assertThrows(IllegalStateException.class,()->payments.review(1L,true,membership.getPaymentReference()));
 }
 @Test void failedPaymentCanBeCorrectedButVerifiedPaymentCannot(){
  payments.submit(1L,"123456789012");payments.review(1L,false,membership.getPaymentReference());
  assertEquals(PaymentStatus.FAILED,membership.getPaymentStatus());
  payments.submit(1L,"123456789013");payments.review(1L,true,membership.getPaymentReference());
  assertThrows(IllegalStateException.class,()->payments.submit(1L,"123456789014"));
 }
}
