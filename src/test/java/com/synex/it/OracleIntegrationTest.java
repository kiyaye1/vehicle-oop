package com.synex.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class OracleIntegrationTest {

  private static final String ORACLE_IMAGE = "gvenzl/oracle-xe:21-slim";
  private static final String ORACLE_PASSWORD = "testpass";

  @Container
  static GenericContainer<?> oracle = new GenericContainer<>(ORACLE_IMAGE)
      .withEnv("ORACLE_PASSWORD", ORACLE_PASSWORD)
      .withExposedPorts(1521)
      .waitingFor(Wait.forLogMessage(".*DATABASE IS READY TO USE!.*", 1));

  @Test
  void containerStarts() {
    Assertions.assertTrue(oracle.isRunning(), "Oracle XE should be running for the test");
  }
}
