package com.purohitcommunity.portal.config;

import com.purohitcommunity.portal.domain.*;
import com.purohitcommunity.portal.domain.Enums.*;
import com.purohitcommunity.portal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Component @Profile("local") @RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
  private final UserAccountRepository users; private final MembershipPlanRepository plans; private final ActivityRepository activities; private final PasswordEncoder encoder;
  @Value("${app.admin.email}") String adminEmail; @Value("${app.admin.password}") String adminPassword;
  @Override public void run(String... args){
    if(!users.existsByEmailIgnoreCase(adminEmail)) users.save(UserAccount.builder().email(adminEmail).passwordHash(encoder.encode(adminPassword)).role(Role.ADMIN).enabled(true).build());
    if(plans.findByCode(PlanCode.PREMIUM).isEmpty()) plans.save(MembershipPlan.builder().code(PlanCode.PREMIUM).nameTelugu("ప్రీమియం సభ్యత్వం").nameEnglish("Premium Membership").price(new BigDecimal("1000.00")).validityMonths(12).active(true).build());
    if(plans.findByCode(PlanCode.ELITE).isEmpty()) plans.save(MembershipPlan.builder().code(PlanCode.ELITE).nameTelugu("ఎలైట్ సభ్యత్వం").nameEnglish("Elite Membership").price(new BigDecimal("2500.00")).validityMonths(12).active(true).build());
    if(activities.count()==0){
      activities.save(Activity.builder().titleTelugu("వేద పారాయణ సమావేశం").titleEnglish("Veda Parayana Gathering").descriptionTelugu("సమాజ సభ్యుల కోసం వేద పారాయణం మరియు సత్సంగం.").descriptionEnglish("Veda recitation and satsang for community members.").venue("Hyderabad").startDateTime(LocalDateTime.now().plusDays(7)).registrationDeadline(LocalDateTime.now().plusDays(5)).capacity(100).allowedPlans(Set.of(PlanCode.PREMIUM,PlanCode.ELITE)).status(ActivityStatus.PUBLISHED).build());
      activities.save(Activity.builder().titleTelugu("ఎలైట్ ఆధ్యాత్మిక సదస్సు").titleEnglish("Elite Spiritual Session").descriptionTelugu("ఎలైట్ సభ్యులకు ప్రత్యేక ఆధ్యాత్మిక సదస్సు.").descriptionEnglish("Special spiritual session for Elite members.").venue("Vijayawada").startDateTime(LocalDateTime.now().plusDays(14)).registrationDeadline(LocalDateTime.now().plusDays(12)).capacity(50).allowedPlans(Set.of(PlanCode.ELITE)).status(ActivityStatus.PUBLISHED).build());
    }
  }
}
