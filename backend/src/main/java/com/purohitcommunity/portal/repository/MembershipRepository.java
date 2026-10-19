package com.purohitcommunity.portal.repository;
import com.purohitcommunity.portal.domain.Membership;
import com.purohitcommunity.portal.domain.Enums.MembershipStatus;
import com.purohitcommunity.portal.domain.Enums.PlanCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface MembershipRepository extends JpaRepository<Membership,Long>{
  Optional<Membership> findFirstByMemberIdOrderByIdDesc(Long memberId);
  long countByStatus(MembershipStatus status);
  @Query("select count(m) from Membership m where m.plan.code=:plan and m.status=:status")
  long countByPlanAndStatus(@Param("plan") PlanCode plan,@Param("status") MembershipStatus status);
}
