package com.careflow.hospitalmanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
class HospitalManagementApplicationTests {

    @Test
    @DisplayName("Application context loads successfully with H2 database and initial seeds")
    void contextLoads() {
    }
}
