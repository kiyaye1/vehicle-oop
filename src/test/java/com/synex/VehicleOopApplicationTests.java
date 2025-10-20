package com.synex;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource("classpath:test-db.properties")
@SpringBootTest(properties = "spring.sql.init.mode=never")
class VehicleOopApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("VehicleOopApplication context loaded successfully using test-db.properties!");
    }
}
