package com.purohitcommunity.portal.repository;
import com.purohitcommunity.portal.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserAccountRepository extends JpaRepository<UserAccount,Long>{
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select u from UserAccount u where u.id = :id")
  Optional<UserAccount> lockById(@org.springframework.data.repository.query.Param("id") Long id);
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  Optional<UserAccount> findByPasswordResetChallenge(String passwordResetChallenge);
  Optional<UserAccount> findByEmailIgnoreCase(String email);
  Optional<UserAccount> findByPhoneNumber(String phoneNumber);
  boolean existsByEmailIgnoreCase(String email);
  boolean existsByPhoneNumber(String phoneNumber);
}
