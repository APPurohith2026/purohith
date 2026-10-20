package com.purohitcommunity.portal.web;
import com.purohitcommunity.portal.domain.Enums.ApplicationStatus;
import com.purohitcommunity.portal.service.PortalService;
import com.purohitcommunity.portal.web.ApiDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
public class AdminController {
  private final PortalService portal;
  private final com.purohitcommunity.portal.storage.PrivateFileStorageService storage;
  @GetMapping("/members/{id}/passport-photo") public org.springframework.http.ResponseEntity<byte[]> passportPhoto(@PathVariable Long id){ return storage.passportPhoto(portal.member(id).passportPhotoPath()); }
  @GetMapping("/dashboard") public AdminDashboard dashboard(){ return portal.adminDashboard(); }
  @GetMapping("/stats") public AdminStats stats(){ return portal.stats(); }
  @GetMapping("/applications") public List<MemberView> applications(@RequestParam(required=false) ApplicationStatus status){ return portal.applications(status); }
  @GetMapping("/applications/{id}") public MemberView application(@PathVariable Long id){ return portal.member(id); }
  @GetMapping("/members") public List<MemberView> members(){ return portal.allMembers(); }
  @GetMapping("/members/{id}") public MemberView member(@PathVariable Long id){ return portal.member(id); }
  @DeleteMapping({"/members/{id}","/applications/{id}"})
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void deleteMember(@PathVariable Long id){ portal.deleteMember(id); }
  @PostMapping("/members/founding") public MemberView founding(@Valid @RequestBody FoundingMemberRequest r){ return portal.createFoundingMember(r); }
  @PatchMapping("/members/{id}/approve") public MemberView approve(@PathVariable Long id,@RequestBody(required=false) ReviewRequest r){ return portal.review(id,ApplicationStatus.APPROVED,r==null?null:r.comment()); }
  @PatchMapping("/members/{id}/reject") public MemberView reject(@PathVariable Long id,@RequestBody(required=false) ReviewRequest r){ return portal.review(id,ApplicationStatus.REJECTED,r==null?null:r.comment()); }
  @PatchMapping("/members/{id}/correction") public MemberView correction(@PathVariable Long id,@RequestBody ReviewRequest r){ return portal.review(id,ApplicationStatus.CORRECTION_REQUIRED,r.comment()); }
  @GetMapping("/activities") public List<ActivityView> activities(){ return portal.adminActivities(); }
  @PostMapping("/activities") public ActivityView create(@Valid @RequestBody ActivityRequest r){ return portal.saveActivity(null,r); }
  @PutMapping("/activities/{id}") public ActivityView update(@PathVariable Long id,@Valid @RequestBody ActivityRequest r){ return portal.saveActivity(id,r); }
  @DeleteMapping("/activities/{id}") public void delete(@PathVariable Long id){ portal.deleteActivity(id); }
}
