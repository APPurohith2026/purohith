package com.purohitcommunity.portal.web;
import com.purohitcommunity.portal.repository.MembershipPlanRepository;
import com.purohitcommunity.portal.service.PortalService;
import com.purohitcommunity.portal.web.ApiDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/public") @RequiredArgsConstructor
public class PublicController {
  private final MembershipPlanRepository plans; private final PortalService portal;
  @Value("${PUBLIC_WEB_URL:http://localhost:5173}") private String publicWebUrl;
  @GetMapping("/plans") public List<PlanView> plans(){ return plans.findByActiveTrueOrderById().stream().map(p->new PlanView(p.getId(),p.getCode(),p.getNameTelugu(),p.getNameEnglish(),p.getPrice(),p.getValidityMonths())).toList(); }
  @GetMapping("/activities") public List<ActivityView> activities(){ return portal.publicActivities(); }
  @GetMapping("/verify/{token}") public IdCardView verify(@PathVariable String token){ return portal.verify(token,publicWebUrl); }
}
