package com.purohitcommunity.portal.repository;

import com.purohitcommunity.portal.domain.MemberProfile;
import com.purohitcommunity.portal.domain.Enums.ApplicationStatus;
import com.purohitcommunity.portal.domain.Enums.RegistrationSource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberProfileRepository
        extends JpaRepository<MemberProfile, Long> {

  Optional<MemberProfile> findByUserId(Long userId);
  List<MemberProfile> findByReferrerMemberId(Long memberId);

  @EntityGraph(attributePaths = "user")
  Optional<MemberProfile> findByMemberNumberIgnoreCase(String memberNumber);

  List<MemberProfile> findByApplicationStatusOrderByCreatedAtAsc(
          ApplicationStatus status
  );

  long countByApplicationStatus(
          ApplicationStatus status
  );

  long countByRegistrationSource(
          RegistrationSource source
  );
}
