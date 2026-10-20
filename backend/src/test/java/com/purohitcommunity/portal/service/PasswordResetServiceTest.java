package com.purohitcommunity.portal.service;

import com.purohitcommunity.portal.domain.*;
import com.purohitcommunity.portal.domain.Enums.Role;
import com.purohitcommunity.portal.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {
    @Mock UserAccountRepository users;
    @Mock MemberProfileRepository members;
    @Mock OtpSmsSender sms;
    final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(4);
    PasswordResetService service;
    UserAccount user;

    @BeforeEach void setup(){
        service=new PasswordResetService(users,members,encoder,sms);
        user=UserAccount.builder().id(1L).role(Role.MEMBER).enabled(true).phoneNumber("9000000001")
            .passwordHash(encoder.encode("OldPassword123!")).build();
    }
    void matchingMember(){
        when(members.findByMemberNumberIgnoreCase("AP-EGD-000001")).thenReturn(Optional.of(MemberProfile.builder().user(user).build()));
        when(users.lockById(1L)).thenReturn(Optional.of(user));
    }
    String request(){matchingMember();return service.request("AP-EGD-000001","9000000001");}
    String capturedOtp(){var otp=ArgumentCaptor.forClass(String.class);verify(sms).send(eq("9000000001"),otp.capture());return otp.getValue();}
    void challenge(String id){when(users.findByPasswordResetChallenge(id)).thenAnswer(inv->id.equals(user.getPasswordResetChallenge())?Optional.of(user):Optional.empty());}

    @Test void verifiesOtpAndChangesPasswordOnce(){
        String id=request(),otp=capturedOtp();challenge(id);
        assertTrue(otp.matches("[0-9]{6}"));
        assertNotEquals(otp,user.getPasswordResetOtpHash());
        String token=service.verify(id,otp);
        assertNotEquals(token,user.getPasswordResetTokenHash());
        assertThrows(IllegalArgumentException.class,()->service.verify(id,otp));
        service.reset(id,token,"NewPassword123!");
        assertTrue(encoder.matches("NewPassword123!",user.getPasswordHash()));
        assertFalse(encoder.matches("OldPassword123!",user.getPasswordHash()));
        assertNull(user.getPasswordResetChallenge());
        assertThrows(IllegalArgumentException.class,()->service.reset(id,token,"AnotherPassword123!"));
    }
    @Test void mismatchedPhoneDoesNotSendOtp(){
        matchingMember();
        assertNotNull(service.request("AP-EGD-000001","9000000002"));
        verify(sms,never()).send(anyString(),anyString());
    }
    @Test void unknownMemberDoesNotSendOtp(){
        when(members.findByMemberNumberIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());
        assertNotNull(service.request("UNKNOWN","9000000001"));
        verify(sms,never()).send(anyString(),anyString());
    }
    @Test void disabledAndAdminAccountsCannotRecover(){
        matchingMember();user.setEnabled(false);
        service.request("AP-EGD-000001","9000000001");
        user.setEnabled(true);user.setRole(Role.ADMIN);
        service.request("AP-EGD-000001","9000000001");
        verify(sms,never()).send(anyString(),anyString());
    }
    @Test void locksOutAfterFiveIncorrectOtps(){
        String id=request(),otp=capturedOtp();challenge(id);
        String wrong=otp.equals("000000")?"111111":"000000";
        for(int i=0;i<5;i++) assertThrows(IllegalArgumentException.class,()->service.verify(id,wrong));
        assertEquals(5,user.getPasswordResetAttempts());
        assertThrows(IllegalArgumentException.class,()->service.verify(id,otp));
    }
    @Test void expiredOtpCannotVerify(){
        String id=request(),otp=capturedOtp();challenge(id);
        user.setPasswordResetExpiresAt(Instant.now().minusSeconds(1));
        assertThrows(IllegalArgumentException.class,()->service.verify(id,otp));
    }
    @Test void cooldownDoesNotReplaceActiveChallenge(){
        String id=request();
        service.request("AP-EGD-000001","9000000001");
        assertEquals(id,user.getPasswordResetChallenge());
        verify(sms,times(1)).send(anyString(),anyString());
    }
    @Test void limitsHourlySmsRequests(){
        matchingMember();user.setPasswordResetWindowStart(Instant.now());user.setPasswordResetSendCount(5);
        service.request("AP-EGD-000001","9000000001");
        verify(sms,never()).send(anyString(),anyString());
    }
    @Test void resendReplacesPreviousChallenge(){
        String old=request();user.setPasswordResetLastSentAt(Instant.now().minusSeconds(61));
        String next=service.request("AP-EGD-000001","9000000001");
        assertNotEquals(old,next);assertEquals(next,user.getPasswordResetChallenge());
        verify(sms,times(2)).send(anyString(),anyString());
    }
    @Test void resetRequiresCorrectUnexpiredToken(){
        String id=request(),otp=capturedOtp();challenge(id);
        String token=service.verify(id,otp);
        assertThrows(IllegalArgumentException.class,()->service.reset(id,"wrong-token","NewPassword123!"));
        assertTrue(encoder.matches("OldPassword123!",user.getPasswordHash()));
        user.setPasswordResetExpiresAt(Instant.now().minusSeconds(1));
        assertThrows(IllegalArgumentException.class,()->service.reset(id,token,"NewPassword123!"));
    }
    @Test void missingGatewayFailsBeforeAccountLookup(){
        doThrow(new IllegalStateException("SMS unavailable")).when(sms).requireAvailable();
        assertThrows(IllegalStateException.class,()->service.request("AP-EGD-000001","9000000001"));
        verifyNoInteractions(members,users);
    }
}
