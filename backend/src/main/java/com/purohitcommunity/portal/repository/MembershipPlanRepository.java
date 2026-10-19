package com.purohitcommunity.portal.repository;
import com.purohitcommunity.portal.domain.MembershipPlan;
import com.purohitcommunity.portal.domain.Enums.PlanCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan,Long>{ Optional<MembershipPlan> findByCode(PlanCode code); List<MembershipPlan> findByActiveTrueOrderById(); }
