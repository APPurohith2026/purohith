package com.purohitcommunity.portal.service;
import com.purohitcommunity.portal.domain.RegistrationDraft;
import com.purohitcommunity.portal.repository.RegistrationDraftRepository;
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
class DraftLoginServiceTest {
 @Mock RegistrationDraftRepository drafts;
 final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(4);
 DraftLoginService service(){return new DraftLoginService(drafts,encoder);}
 RegistrationDraft saved(){return RegistrationDraft.builder().draftToken("secret").draftLoginPhone("9000000001").passwordHash(encoder.encode("DraftPassword123!")).lastCompletedStep(2).build();}
 @Test void stepTwoHashesPasswordAndFinalSubmissionReusesIt(){
  var d=RegistrationDraft.builder().draftToken("secret").build();
  service().configure(d,"9000000001","DraftPassword123!");
  assertNotEquals("DraftPassword123!",d.getPasswordHash());assertTrue(encoder.matches("DraftPassword123!",d.getPasswordHash()));
  assertEquals(d.getPasswordHash(),service().passwordHashForSubmission(d,null));
  assertEquals(d.getPasswordHash(),service().passwordHashForSubmission(d,"DifferentPassword123!"));
 }
 @Test void newDraftCannotOmitPassword(){
  assertThrows(IllegalArgumentException.class,()->service().configure(new RegistrationDraft(),"9000000001",null));
  assertThrows(IllegalArgumentException.class,()->service().passwordHashForSubmission(new RegistrationDraft(),null));
 }
 @Test void anotherDraftCannotClaimExistingDraftPhone(){
  when(drafts.findByDraftLoginPhoneAndSubmittedFalse("9000000001")).thenReturn(Optional.of(saved()));
  assertThrows(IllegalArgumentException.class,()->service().configure(RegistrationDraft.builder().draftToken("other").build(),"9000000001","Password123!"));
 }
 @Test void successfulLoginReturnsExistingDraftAndClearsFailures(){
  var d=saved();d.setLoginFailures(2);
  when(drafts.findByDraftLoginPhoneAndSubmittedFalse("9000000001")).thenReturn(Optional.of(d));when(drafts.save(d)).thenReturn(d);
  assertSame(d,service().authenticate("90000-00001","DraftPassword123!"));assertEquals(0,d.getLoginFailures());
 }
 @Test void wrongPasswordNeverReturnsDraftAndLocksAfterFiveAttempts(){
  var d=saved();when(drafts.findByDraftLoginPhoneAndSubmittedFalse("9000000001")).thenReturn(Optional.of(d));
  for(int i=0;i<5;i++)assertThrows(DraftLoginService.DraftLoginException.class,()->service().authenticate("9000000001","WrongPassword"));
  assertTrue(d.getLoginLockedUntil().isAfter(Instant.now()));
  assertThrows(DraftLoginService.DraftLoginException.class,()->service().authenticate("9000000001","DraftPassword123!"));
  verify(drafts,times(5)).save(d);
 }
 @Test void unknownAndSubmittedDraftsCannotLogin(){
  assertThrows(DraftLoginService.DraftLoginException.class,()->service().authenticate("9000000001","Password123!"));
 }
 @Test void resumedDraftDoesNotRequirePasswordAgainWhenSavingAddress(){
  var d=saved();String hash=d.getPasswordHash();
  when(drafts.findByDraftLoginPhoneAndSubmittedFalse("9000000001")).thenReturn(Optional.of(d));
  service().configure(d,"9000000001",null);assertEquals(hash,d.getPasswordHash());
 }
}
