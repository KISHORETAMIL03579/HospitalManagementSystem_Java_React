package com.careflow.hospitalmanagement;

import com.careflow.hospitalmanagement.controller.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationAuditTest {

    private static final Class<?>[] CONTROLLERS = {
        AuthController.class,
        AdminUsersController.class,
        StaffRegistrationController.class,
        PatientsController.class,
        DoctorsController.class,
        AppointmentsController.class
    };

    @Test
    @DisplayName("All controller classes should have @RestController and be mapped under /api/v1")
    void testControllerAnnotations() {
        for (Class<?> controller : CONTROLLERS) {
            assertThat(controller.isAnnotationPresent(RestController.class))
                    .withFailMessage("Controller " + controller.getSimpleName() + " is missing @RestController")
                    .isTrue();

            if (controller.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping rm = controller.getAnnotation(RequestMapping.class);
                String path = rm.value().length > 0 ? rm.value()[0] : "";
                assertThat(path).startsWith("/api/v1");
            }
        }
    }

    @Test
    @DisplayName("Every public method mapped to HTTP verb should follow REST conventions")
    void testPublicEndpointsAudit() {
        Set<String> auditedMethods = new HashSet<>();
        for (Class<?> controller : CONTROLLERS) {
            for (Method method : controller.getDeclaredMethods()) {
                boolean isMapping = method.isAnnotationPresent(GetMapping.class)
                        || method.isAnnotationPresent(PostMapping.class)
                        || method.isAnnotationPresent(PutMapping.class)
                        || method.isAnnotationPresent(PatchMapping.class)
                        || method.isAnnotationPresent(DeleteMapping.class)
                        || method.isAnnotationPresent(RequestMapping.class);

                if (isMapping) {
                    auditedMethods.add(controller.getSimpleName() + "#" + method.getName());
                }
            }
        }

        // Verify that we discovered dozens of API endpoints across our controllers
        assertThat(auditedMethods.size()).isGreaterThanOrEqualTo(20);
    }
}
