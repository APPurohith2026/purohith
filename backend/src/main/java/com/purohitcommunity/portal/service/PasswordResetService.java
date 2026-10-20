package com.purohitcommunity.portal.service;

import com.purohitcommunity.portal.domain.UserAccount;
import com.purohitcommunity.portal.domain.Enums.Role;
import com.purohitcommunity.portal.repository.MemberProfileRepository;
import com.purohitcommunity.portal.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class PasswordResetService {
    private final UserAccountRepository users;
    private final MemberProfileRepository members;
    private final PasswordEncoder encoder;
    private final OtpSmsSender sms;
    private final SecureRandom random=new SecureRandom();

    @Transactional public String request(String memberId,String phoneNumber){
        sms.requireAvailable();
        String challenge=UUID.randomUUID().toString();
        var member=members.findByMemberNumberIgnoreCase(memberId.trim()).orElse(null);
        if(member==null) return challenge;
        var user=users.lockById(member.getUser().getId()).orElse(null);
        String phone=phoneNumber.replaceAll("\\D", "");
        if(!eligible(user) || !phone.equals(user.getPhoneNumber())) return challenge;
        Instant now=Instant.now();
        if(user.getPasswordResetLastSentAt()!=null && now.isBefore(user.getPasswordResetLastSentAt().plusSeconds(60)))
            return challenge;
        if(user.getPasswordResetWindowStart()==null || !now.isBefore(user.getPasswordResetWindowStart().plusSeconds(3600))){
            user.setPasswordResetWindowStart(now);
            user.setPasswordResetSendCount(0);
        }
        if(user.getPasswordResetSendCount()>=5) return challenge;
        String otp=String.format("%06d",random.nextInt(1_000_000));
        user.setPasswordResetChallenge(challenge);
        user.setPasswordResetOtpHash(encoder.encode(otp));
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(now.plusSeconds(300));
        user.setPasswordResetAttempts(0);
        user.setPasswordResetLastSentAt(now);
        user.setPasswordResetSendCount(user.getPasswordResetSendCount()+1);
        sms.send(user.getPhoneNumber(),otp);
        return challenge;
    }

    // Failed attempts must commit so an attacker cannot roll back the attempt limit.
    @Transactional(noRollbackFor=IllegalArgumentException.class)
    public String verify(String challenge,String otp){
        var user=users.findByPasswordResetChallenge(challenge).orElseThrow(PasswordResetService::invalid);
        check(user);
        if(user.getPasswordResetOtpHash()==null || user.getPasswordResetAttempts()>=5) throw invalid();
        user.setPasswordResetAttempts(user.getPasswordResetAttempts()+1);
        if(!encoder.matches(otp,user.getPasswordResetOtpHash())) throw invalid();
        String token=UUID.randomUUID().toString();
        user.setPasswordResetTokenHash(encoder.encode(token));
        user.setPasswordResetOtpHash(null);
        user.setPasswordResetExpiresAt(Instant.now().plusSeconds(300));
        user.setPasswordResetAttempts(0);
        return token;
    }

    @Transactional(noRollbackFor=IllegalArgumentException.class)
    public void reset(String challenge,String token,String password){
        // BCrypt accepts at most 72 bytes; constrain API passwords to that limit.
        if(password.length()<8 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("Password must be at least 8 characters and at most 72 UTF-8 bytes.");
        var user=users.findByPasswordResetChallenge(challenge).orElseThrow(PasswordResetService::invalid);
        check(user);
        if(user.getPasswordResetTokenHash()==null || user.getPasswordResetAttempts()>=5) throw invalid();
        user.setPasswordResetAttempts(user.getPasswordResetAttempts()+1);
        if(!encoder.matches(token,user.getPasswordResetTokenHash())) throw invalid();
        user.setPasswordHash(encoder.encode(password));
        user.setPasswordResetChallenge(null);
        user.setPasswordResetOtpHash(null);
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(null);
    }

    private static boolean eligible(UserAccount user){return user!=null && user.isEnabled() && user.getRole()==Role.MEMBER;}
    private static void check(UserAccount user){
        if(!eligible(user) || user.getPasswordResetExpiresAt()==null || !Instant.now().isBefore(user.getPasswordResetExpiresAt())) throw invalid();
    }
    private static IllegalArgumentException invalid(){return new IllegalArgumentException("Invalid or expired verification. Request a new OTP.");}
}
