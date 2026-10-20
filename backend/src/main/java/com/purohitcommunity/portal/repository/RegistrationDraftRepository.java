package com.purohitcommunity.portal.repository;
import com.purohitcommunity.portal.domain.RegistrationDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RegistrationDraftRepository extends JpaRepository<RegistrationDraft,Long> {
    Optional<RegistrationDraft> findByDraftTokenAndSubmittedFalse(String draftToken);
    long countBySubmittedFalse();
    void deleteByPhoneNumber(String phoneNumber);
}
