package com.purohitcommunity.portal.web;

import com.purohitcommunity.portal.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth/password-reset") @RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService reset;
    public record OtpRequest(@NotBlank @Size(max=40) String memberId,
                             @NotBlank @Pattern(regexp="[+0-9 ()-]{10,25}") String phoneNumber){}
    public record VerifyRequest(@NotBlank @Size(max=36) String challengeId,
                                @NotBlank @Pattern(regexp="[0-9]{6}") String otp){}
    public record ResetRequest(@NotBlank @Size(max=36) String challengeId,
                               @NotBlank @Size(max=36) String resetToken,
                               @NotBlank @Size(min=8,max=72) String newPassword){}

    @PostMapping("/request") public Map<String,String> request(@Valid @RequestBody OtpRequest r){
        return Map.of("challengeId",reset.request(r.memberId(),r.phoneNumber()),
            "message","If the Member ID and registered phone match, an OTP will be sent. It expires in 5 minutes.");
    }
    @PostMapping("/verify") public Map<String,String> verify(@Valid @RequestBody VerifyRequest r){
        return Map.of("resetToken",reset.verify(r.challengeId(),r.otp()));
    }
    @PostMapping("/confirm") public Map<String,String> confirm(@Valid @RequestBody ResetRequest r){
        reset.reset(r.challengeId(),r.resetToken(),r.newPassword());
        return Map.of("message","Password updated. Log in with your Member ID or registered phone and new password.");
    }
}
