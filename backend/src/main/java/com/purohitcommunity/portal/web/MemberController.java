package com.purohitcommunity.portal.web;
import com.purohitcommunity.portal.service.PortalService;
import com.purohitcommunity.portal.service.QrCodeService;
import com.purohitcommunity.portal.web.ApiDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.util.*;

@RestController @RequestMapping("/api/member") @RequiredArgsConstructor
public class MemberController {
  private final PortalService portal; private final QrCodeService qr;
  private final com.purohitcommunity.portal.service.MembershipPaymentService payments;
  public record PaymentReferenceRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=64) String reference) {}
  @GetMapping("/payment") public com.purohitcommunity.portal.service.MembershipPaymentService.PaymentView payment(Authentication a){ return payments.get(portal.current(a.getName()).getId()); }
  @PostMapping("/payment") public com.purohitcommunity.portal.service.MembershipPaymentService.PaymentView payment(Authentication a,@Valid @RequestBody PaymentReferenceRequest r){ return payments.submit(portal.current(a.getName()).getId(),r.reference()); }
  private final com.purohitcommunity.portal.storage.PrivateFileStorageService storage;
  @GetMapping("/passport-photo") public ResponseEntity<byte[]> passportPhoto(Authentication a){ return storage.passportPhoto(portal.current(a.getName()).getPassportPhotoPath()); }
  @Value("${PUBLIC_WEB_URL:http://localhost:5173}") private String publicWebUrl;
  @GetMapping("/profile") public MemberView profile(Authentication a){ return portal.memberView(portal.current(a.getName())); }
  @PutMapping("/profile") public MemberView update(Authentication a,@Valid @RequestBody ProfileUpdateRequest r){ return portal.updateProfile(a.getName(),r); }
  @PostMapping("/enroll") public MemberView enroll(Authentication a,@Valid @RequestBody EnrollmentRequest r){ return portal.enroll(a.getName(),r); }
  @GetMapping("/dashboard") public DashboardView dashboard(Authentication a){ return portal.dashboard(a.getName()); }
  @GetMapping("/activities") public List<ActivityView> activities(Authentication a){ return portal.memberActivities(a.getName()); }
  @PostMapping("/activities/{id}/register") public Map<String,String> register(Authentication a,@PathVariable Long id){ portal.registerActivity(a.getName(),id); return Map.of("message","Registered successfully"); }
  @GetMapping("/id-card") public IdCardView idCard(Authentication a){ return portal.card(a.getName(),publicWebUrl); }
  @GetMapping(value="/id-card/qr", produces=MediaType.IMAGE_PNG_VALUE) public ResponseEntity<byte[]> qr(Authentication a){ var c=portal.card(a.getName(),publicWebUrl); return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qr.png(c.verificationUrl())); }
}
