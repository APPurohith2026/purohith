package com.purohitcommunity.portal.integration;

import com.purohitcommunity.portal.repository.MemberProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("test")
class MySqlContainerSmokeTest {
  @Container static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
      .withDatabaseName("purohit_test").withUsername("test").withPassword("test");

  @DynamicPropertySource
  static void db(DynamicPropertyRegistry r){
    r.add("spring.datasource.url", MYSQL::getJdbcUrl);
    r.add("spring.datasource.username", MYSQL::getUsername);
    r.add("spring.datasource.password", MYSQL::getPassword);
    r.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
  }

  @Autowired MemberProfileRepository members;
  @Autowired com.purohitcommunity.portal.repository.UserAccountRepository users;
  @Autowired com.purohitcommunity.portal.service.PasswordResetService passwordReset;
  @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
  @org.springframework.test.context.bean.override.mockito.MockitoBean
  com.purohitcommunity.portal.service.OtpSmsSender sms;

  @Test void failedOtpAttemptsCommitAndSuccessfulResetCannotBeReplayed(){
    var user=users.save(com.purohitcommunity.portal.domain.UserAccount.builder()
        .phoneNumber("9000000001").role(com.purohitcommunity.portal.domain.Enums.Role.MEMBER)
        .enabled(true).passwordHash(encoder.encode("OldPassword123!")).build());
    var member=members.save(com.purohitcommunity.portal.domain.MemberProfile.builder()
        .user(user).memberNumber("AP-EGD-000001").build());
    try{
      String id=passwordReset.request("AP-EGD-000001","9000000001");
      var captured=org.mockito.ArgumentCaptor.forClass(String.class);
      org.mockito.Mockito.verify(sms).send(org.mockito.ArgumentMatchers.eq("9000000001"),captured.capture());
      String otp=captured.getValue(),wrong=otp.equals("000000")?"111111":"000000";
      org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,()->passwordReset.verify(id,wrong));
      assertEquals(1,users.findById(user.getId()).orElseThrow().getPasswordResetAttempts());
      String token=passwordReset.verify(id,otp);
      passwordReset.reset(id,token,"NewPassword123!");
      org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("NewPassword123!",users.findById(user.getId()).orElseThrow().getPasswordHash()));
      org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,()->passwordReset.reset(id,token,"ReplayPassword123!"));
    }finally{
      members.deleteById(member.getId());
      users.deleteById(user.getId());
    }
  }

  @Test void contextAndRepositoryWorkAgainstRealMySql(){
    assertEquals(0L, members.count());
  }
}
