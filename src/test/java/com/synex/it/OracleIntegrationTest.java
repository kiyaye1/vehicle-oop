package com.synex.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OracleIntegrationTest {

  private static final String ORACLE_IMAGE = "gvenzl/oracle-xe:21-slim";
  private static final String ORACLE_PASSWORD = "testpass";

  @Container
  static GenericContainer<?> oracle = new GenericContainer<>(ORACLE_IMAGE)
      .withEnv("ORACLE_PASSWORD", ORACLE_PASSWORD)
      .withExposedPorts(1521)
      .waitingFor(Wait.forLogMessage(".*DATABASE IS READY TO USE!.*", 1));

  @DynamicPropertySource
  static void dbProps(DynamicPropertyRegistry registry) {
    int port = oracle.getMappedPort(1521);
    String url = "jdbc:oracle:thin:@//localhost:" + port + "/XEPDB1";
    registry.add("spring.datasource.url", () -> url);
    registry.add("spring.datasource.username", () -> "system");
    registry.add("spring.datasource.password", () -> ORACLE_PASSWORD);
    registry.add("spring.datasource.driver-class-name", () -> "oracle.jdbc.OracleDriver");
  }

  @Test
  void containerStarts() {
    Assertions.assertTrue(oracle.isRunning(), "Oracle XE should be running for the test");
  }
}
