package com.purohitcommunity.portal.repository;
import com.purohitcommunity.portal.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserAccountRepository extends JpaRepository<UserAccount,Long>{
  Optional<UserAccount> findByEmailIgnoreCase(String email);
  Optional<UserAccount> findByPhoneNumber(String phoneNumber);
  boolean existsByEmailIgnoreCase(String email);
  boolean existsByPhoneNumber(String phoneNumber);
}
