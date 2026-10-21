package com.purohitcommunity.portal.service;
import com.purohitcommunity.portal.domain.RegistrationDraft;
import com.purohitcommunity.portal.repository.RegistrationDraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service @RequiredArgsConstructor
public class DraftLoginService {
 private final RegistrationDraftRepository drafts;
 private final PasswordEncoder encoder;
 public static class DraftLoginException extends IllegalArgumentException {
  public DraftLoginException(){super("Unable to resume registration. Check your phone number and password, or try again later.");}
 }
 public String passwordHashForSubmission(RegistrationDraft draft,String password){
  if(draft.getPasswordHash()!=null)return draft.getPasswordHash();
  if(password==null || password.isBlank() || password.length()<8 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
   throw new IllegalArgumentException("Return to step 1 and set your registration password");
  return encoder.encode(password);
 }
 public void configure(RegistrationDraft draft,String phone,String password){
  var other=drafts.findByDraftLoginPhoneAndSubmittedFalse(phone).orElse(null);
  if(other!=null && !other.getDraftToken().equals(draft.getDraftToken())) throw new IllegalArgumentException("A saved registration already exists for this phone number. Log in with your phone number and password to continue it.");
  if(password!=null){
   if(password.isBlank() || password.length()<8 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) throw new IllegalArgumentException("Use a password of at least 8 characters, up to 72 UTF-8 bytes");
   draft.setPasswordHash(encoder.encode(password));
  }
  if(draft.getPasswordHash()==null) throw new IllegalArgumentException("Set a password to save and resume your registration");
  draft.setDraftLoginPhone(phone);draft.setLoginFailures(0);draft.setLoginLockedUntil(null);
 }
 @Transactional(noRollbackFor=DraftLoginException.class)
 public RegistrationDraft authenticate(String phone,String password){
  String normalized=phone==null?"":phone.replaceAll("\\D","");
  var draft=drafts.findByDraftLoginPhoneAndSubmittedFalse(normalized).orElseThrow(DraftLoginException::new);
  if(draft.getLoginLockedUntil()!=null && draft.getLoginLockedUntil().isAfter(Instant.now())) throw new DraftLoginException();
  if(password==null || !encoder.matches(password,draft.getPasswordHash())){
   int failures=draft.getLoginFailures()==null?1:draft.getLoginFailures()+1;
   draft.setLoginFailures(failures);
   if(failures>=5){draft.setLoginLockedUntil(Instant.now().plusSeconds(900));draft.setLoginFailures(0);}
   drafts.save(draft);throw new DraftLoginException();
  }
  draft.setLoginFailures(0);draft.setLoginLockedUntil(null);return drafts.save(draft);
 }
}
