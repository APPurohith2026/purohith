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

  @Test void contextAndRepositoryWorkAgainstRealMySql(){
    assertEquals(0L, members.count());
  }
}
