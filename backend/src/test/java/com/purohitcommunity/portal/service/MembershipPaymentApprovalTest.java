package com.purohitcommunity.portal.service;
import com.purohitcommunity.portal.domain.*;
import com.purohitcommunity.portal.domain.Enums.*;
import com.purohitcommunity.portal.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class MembershipPaymentApprovalTest {
 @Mock MemberProfileRepository members;
 @Mock MembershipRepository memberships;
 @InjectMocks PortalService portal;
 @Test void referenceSubmissionDoesNotAllowMembershipApproval(){
  when(members.findById(1L)).thenReturn(Optional.of(new MemberProfile()));
  when(memberships.findFirstByMemberIdOrderByIdDesc(1L)).thenReturn(Optional.of(Membership.builder().paymentTrackingId("PAY-test").paymentStatus(PaymentStatus.MANUAL_REVIEW).build()));
  assertThrows(IllegalStateException.class,()->portal.review(1L,ApplicationStatus.APPROVED,null));
  verify(members,never()).save(any());
 }
}
