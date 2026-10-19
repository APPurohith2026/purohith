package com.purohitcommunity.portal.repository;
import com.purohitcommunity.portal.domain.ActivityRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration,Long>{ boolean existsByActivityIdAndMemberId(Long activityId, Long memberId); long countByActivityId(Long activityId); List<ActivityRegistration> findByMemberIdOrderByRegisteredAtDesc(Long memberId); }
