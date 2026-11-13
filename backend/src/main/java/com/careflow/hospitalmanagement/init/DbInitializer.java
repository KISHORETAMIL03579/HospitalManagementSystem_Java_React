package com.careflow.hospitalmanagement.init;

import com.careflow.hospitalmanagement.common.RolePermissions.Permissions;
import com.careflow.hospitalmanagement.domain.entities.*;
import com.careflow.hospitalmanagement.domain.enums.*;
import com.careflow.hospitalmanagement.repository.*;
import com.careflow.hospitalmanagement.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
public class DbInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DbInitializer.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvitationCodeRepository invitationCodeRepository;
    private final StaffRegistrationRequestRepository staffRegistrationRequestRepository;
    private final EmailLogRepository emailLogRepository;
    private final PasswordHasher passwordHasher;

    public DbInitializer(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            DoctorRepository doctorRepository,
            DoctorScheduleRepository doctorScheduleRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            InvitationCodeRepository invitationCodeRepository,
            StaffRegistrationRequestRepository staffRegistrationRequestRepository,
            EmailLogRepository emailLogRepository,
            PasswordHasher passwordHasher) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.invitationCodeRepository = invitationCodeRepository;
        this.staffRegistrationRequestRepository = staffRegistrationRequestRepository;
        this.emailLogRepository = emailLogRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking dynamic RBAC database schema and seed status...");

        // 1. Seed Roles
        if (roleRepository.count() == 0) {
            log.info("Seeding dynamic Roles hierarchy...");

            Role admin = roleRepository.save(new Role("Admin", "System Super Administrator with full operational access", 100));
            Role manager = roleRepository.save(new Role("HospitalManager", "Hospital Operations Manager", 90));
            manager.setParentRoleId(admin.getRoleId());
            roleRepository.save(manager);

            Role receptionist = roleRepository.save(new Role("Receptionist", "Patient Intake & Front Desk Scheduling", 40));
            receptionist.setParentRoleId(admin.getRoleId());
            roleRepository.save(receptionist);

            Role doctor = roleRepository.save(new Role("Doctor", "Attending Physician & Specialist", 80));
            doctor.setParentRoleId(manager.getRoleId());
            roleRepository.save(doctor);

            Role pharmacist = roleRepository.save(new Role("Pharmacist", "Pharmacy & Medication Dispensing Manager", 70));
            pharmacist.setParentRoleId(manager.getRoleId());
            roleRepository.save(pharmacist);

            Role labTech = roleRepository.save(new Role("LabTechnician", "Laboratory Technician & Diagnostics Specialist", 70));
            labTech.setParentRoleId(manager.getRoleId());
            roleRepository.save(labTech);

            Role patient = roleRepository.save(new Role("Patient", "Registered Patient", 20));
            patient.setParentRoleId(receptionist.getRoleId());
            roleRepository.save(patient);

            Role nurse = roleRepository.save(new Role("Nurse", "Clinical Care Nurse", 60));
            nurse.setParentRoleId(doctor.getRoleId());
            roleRepository.save(nurse);

            log.info("Dynamic Roles seeded successfully.");
        }

        // 2. Seed Granular Permissions & RolePermissions
        if (permissionRepository.count() == 0) {
            log.info("Seeding granular System Permissions...");

            List<Permission> perms = List.of(
                new Permission(Permissions.ManageUsers, "Administration", "Create and manage system user accounts"),
                new Permission(Permissions.ManageSystem, "Administration", "System configuration and maintenance"),
                new Permission(Permissions.ViewPatient, "Patients", "View patient records and demographics"),
                new Permission(Permissions.RegisterPatient, "Patients", "Register new patient records"),
                new Permission(Permissions.ViewOwnProfile, "Patients", "View personal patient profile"),
                new Permission(Permissions.CreateEncounter, "Clinical", "Create medical encounters and diagnostic notes"),
                new Permission(Permissions.CreatePrescription, "Clinical", "Prescribe medications"),
                new Permission(Permissions.ViewLabResult, "Clinical", "View patient lab results"),
                new Permission(Permissions.EnterLabResult, "Laboratory", "Enter and certify lab diagnostic results"),
                new Permission(Permissions.RecordVitals, "Clinical", "Record patient vital signs"),
                new Permission(Permissions.CreateAppointment, "Scheduling", "Schedule doctor consultations"),
                new Permission(Permissions.ViewAppointment, "Scheduling", "View appointment schedules"),
                new Permission(Permissions.CheckInPatient, "Scheduling", "Check in patients for visits"),
                new Permission(Permissions.DispenseMedication, "Pharmacy", "Fulfill and dispense prescriptions"),
                new Permission(Permissions.ViewOwnAppointments, "Patients", "View personal appointments"),
                new Permission(Permissions.ViewOwnLabResults, "Patients", "View personal lab diagnostic reports")
            );

            permissionRepository.saveAll(perms);

            Map<String, Role> roleMap = new HashMap<>();
            roleRepository.findAll().forEach(r -> roleMap.put(r.getName(), r));

            Map<String, Permission> permMap = new HashMap<>();
            permissionRepository.findAll().forEach(p -> permMap.put(p.getName(), p));

            List<RolePermission> rolePerms = new ArrayList<>();

            // Admin gets all permissions
            Role adminRole = roleMap.get("Admin");
            for (Permission p : permMap.values()) {
                rolePerms.add(new RolePermission(adminRole.getRoleId(), p.getPermissionId()));
            }

            // Doctor
            Role docRole = roleMap.get("Doctor");
            for (String name : List.of(Permissions.ViewPatient, Permissions.CreateEncounter, Permissions.CreatePrescription, Permissions.ViewLabResult, Permissions.ViewAppointment, Permissions.RecordVitals)) {
                if (permMap.containsKey(name)) rolePerms.add(new RolePermission(docRole.getRoleId(), permMap.get(name).getPermissionId()));
            }

            // Nurse
            Role nurseRole = roleMap.get("Nurse");
            for (String name : List.of(Permissions.ViewPatient, Permissions.RecordVitals, Permissions.ViewAppointment, Permissions.ViewLabResult)) {
                if (permMap.containsKey(name)) rolePerms.add(new RolePermission(nurseRole.getRoleId(), permMap.get(name).getPermissionId()));
            }

            // Receptionist
            Role receptRole = roleMap.get("Receptionist");
            for (String name : List.of(Permissions.RegisterPatient, Permissions.CreateAppointment, Permissions.ViewAppointment, Permissions.CheckInPatient, Permissions.ViewPatient)) {
                if (permMap.containsKey(name)) rolePerms.add(new RolePermission(receptRole.getRoleId(), permMap.get(name).getPermissionId()));
            }

            // Patient
            Role patRole = roleMap.get("Patient");
            for (String name : List.of(Permissions.ViewOwnProfile, Permissions.ViewOwnAppointments, Permissions.ViewOwnLabResults)) {
                if (permMap.containsKey(name)) rolePerms.add(new RolePermission(patRole.getRoleId(), permMap.get(name).getPermissionId()));
            }

            // Pharmacist
            Role pharmaRole = roleMap.get("Pharmacist");
            for (String name : List.of(Permissions.ViewPatient, Permissions.DispenseMedication, Permissions.ViewLabResult)) {
                if (permMap.containsKey(name)) rolePerms.add(new RolePermission(pharmaRole.getRoleId(), permMap.get(name).getPermissionId()));
            }

            // LabTechnician
            Role labRole = roleMap.get("LabTechnician");
            for (String name : List.of(Permissions.ViewPatient, Permissions.EnterLabResult, Permissions.ViewLabResult)) {
                if (permMap.containsKey(name)) rolePerms.add(new RolePermission(labRole.getRoleId(), permMap.get(name).getPermissionId()));
            }

            rolePermissionRepository.saveAll(rolePerms);
            log.info("RolePermissions mapped successfully.");
        }

        // 3. Seed Users
        if (userRepository.count() == 0) {
            log.info("Seeding user accounts for all dynamic roles...");
            Map<String, Role> roleMap = new HashMap<>();
            roleRepository.findAll().forEach(r -> roleMap.put(r.getName(), r));

            List<User> users = List.of(
                createUser("admin", "admin@careflow.com", "System Administrator", "Admin123!", roleMap.get("Admin")),
                createUser("doctor", "doctor@careflow.com", "Dr. Eleanor Vance", "Doctor123!", roleMap.get("Doctor")),
                createUser("nurse", "nurse@careflow.com", "Nurse Clara Barton", "Nurse123!", roleMap.get("Nurse")),
                createUser("reception", "reception@careflow.com", "Receptionist Staff", "Reception123!", roleMap.get("Receptionist")),
                createUser("patient", "patient@careflow.com", "Patient John Smith", "Patient123!", roleMap.get("Patient")),
                createUser("pharmacist", "pharmacist@careflow.com", "Pharm. Robert Ford", "Pharma123!", roleMap.get("Pharmacist")),
                createUser("labtech", "labtech@careflow.com", "Tech. Marie Curie", "LabTech123!", roleMap.get("LabTechnician"))
            );

            userRepository.saveAll(users);
            log.info("Dynamic User seeding completed successfully.");
        }

        // 4. Seed Departments & Doctors
        if (departmentRepository.count() == 0) {
            log.info("Seeding initial Departments and Doctors...");

            Department cardio = departmentRepository.save(new Department("Cardiology", "CARDIO", "Heart & Cardiovascular Care"));
            Department neuro = departmentRepository.save(new Department("Neurology", "NEURO", "Brain & Nervous System Care"));
            Department ortho = departmentRepository.save(new Department("Orthopedics", "ORTHO", "Bones, Joints & Musculoskeletal System"));
            Department pedia = departmentRepository.save(new Department("Pediatrics", "PEDIA", "Child and Adolescent Care"));

            List<Doctor> doctors = List.of(
                createDoctor("DOC-1001", "Eleanor", "Vance", "Cardiologist", "dr.vance@careflow.com", "+1 (555) 234-5678", cardio, BigDecimal.valueOf(150.0)),
                createDoctor("DOC-1002", "Marcus", "Brody", "Neurologist", "dr.brody@careflow.com", "+1 (555) 876-5432", neuro, BigDecimal.valueOf(180.0)),
                createDoctor("DOC-1003", "Sarah", "Connor", "Orthopedic Surgeon", "dr.connor@careflow.com", "+1 (555) 345-6789", ortho, BigDecimal.valueOf(200.0)),
                createDoctor("DOC-1004", "Julian", "Bashir", "Pediatrician", "dr.bashir@careflow.com", "+1 (555) 987-6543", pedia, BigDecimal.valueOf(120.0))
            );

            doctorRepository.saveAll(doctors);
            log.info("Department and Doctor seeding completed successfully.");
        }

        // 4b. Seed Doctor Schedules
        if (doctorScheduleRepository.count() == 0) {
            List<Doctor> doctors = doctorRepository.findAll();
            List<DoctorSchedule> schedules = new ArrayList<>();

            for (Doctor doc : doctors) {
                for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    DoctorSchedule morning = new DoctorSchedule();
                    morning.setDoctorId(doc.getDoctorId());
                    morning.setDoctor(doc);
                    morning.setDayOfWeek(day);
                    morning.setStartTime(LocalTime.of(9, 0));
                    morning.setEndTime(LocalTime.of(12, 30));
                    morning.setSlotDurationMinutes(30);
                    morning.setSessionName("Morning Clinic");
                    morning.setActive(true);
                    schedules.add(morning);

                    DoctorSchedule afternoon = new DoctorSchedule();
                    afternoon.setDoctorId(doc.getDoctorId());
                    afternoon.setDoctor(doc);
                    afternoon.setDayOfWeek(day);
                    afternoon.setStartTime(LocalTime.of(13, 30));
                    afternoon.setEndTime(LocalTime.of(17, 0));
                    afternoon.setSlotDurationMinutes(30);
                    afternoon.setSessionName("Afternoon Clinic");
                    afternoon.setActive(true);
                    schedules.add(afternoon);
                }
            }

            doctorScheduleRepository.saveAll(schedules);
            log.info("Seeded {} DoctorSchedules migrated from doctor profiles.", schedules.size());
        }

        // 5. Seed Patients
        if (patientRepository.count() == 0) {
            log.info("Seeding initial Sample Patients...");

            List<Patient> patients = List.of(
                createPatient("MRN-2026-0001", "John", "Smith", LocalDate.of(1985, 4, 12), Gender.Male, BloodGroup.OPositive, "+1 (555) 111-2222", "john.smith@careflow.com", "123 Maple Street, New York, NY", "Mary Smith", "+1 (555) 111-9999"),
                createPatient("MRN-2026-0002", "Emma", "Watson", LocalDate.of(1992, 9, 18), Gender.Female, BloodGroup.APositive, "+1 (555) 222-3333", "emma.watson@careflow.com", "456 Oak Avenue, Boston, MA", "Arthur Watson", "+1 (555) 222-8888"),
                createPatient("MRN-2026-0003", "Robert", "Downey", LocalDate.of(1975, 1, 25), Gender.Male, BloodGroup.BPositive, "+1 (555) 333-4444", "robert.downey@careflow.com", "789 Pine Road, Los Angeles, CA", "Susan Downey", "+1 (555) 333-7777"),
                createPatient("MRN-2026-0004", "Sophia", "Martinez", LocalDate.of(1998, 6, 30), Gender.Female, BloodGroup.ABPositive, "+1 (555) 444-5555", "sophia.martinez@careflow.com", "321 Cedar Lane, Chicago, IL", "Carlos Martinez", "+1 (555) 444-6666"),
                createPatient("MRN-2026-0005", "David", "Beckham", LocalDate.of(1980, 11, 5), Gender.Male, BloodGroup.ONegative, "+1 (555) 555-6666", "david.beckham@careflow.com", "654 Elm Street, Miami, FL", "Victoria Beckham", "+1 (555) 555-5555"),
                createPatient("MRN-2026-0006", "Grace", "Hopper", LocalDate.of(1990, 12, 9), Gender.Female, BloodGroup.ANegative, "+1 (555) 666-7777", "grace.hopper@careflow.com", "987 Birch Drive, Seattle, WA", "Admiral Hopper", "+1 (555) 666-4444")
            );

            patientRepository.saveAll(patients);
            log.info("Sample Patients seeded successfully.");
        }

        // 6. Seed Appointments
        if (appointmentRepository.count() == 0) {
            log.info("Seeding initial Sample Appointments...");

            List<Patient> patients = patientRepository.findAll();
            List<Doctor> doctors = doctorRepository.findAll();

            if (!patients.isEmpty() && !doctors.isEmpty()) {
                Patient p1 = patients.get(0);
                Patient p2 = patients.get(1);
                Patient p3 = patients.get(2);
                Patient p4 = patients.get(3);
                Patient p5 = patients.get(4);
                Patient p6 = patients.get(5);

                Doctor d1 = doctors.get(0);
                Doctor d2 = doctors.get(1);
                Doctor d3 = doctors.get(2);
                Doctor d4 = doctors.get(3);

                LocalDate today = LocalDate.now();

                List<Appointment> appts = List.of(
                    createAppointment(p1, d1, today, LocalTime.of(9, 30), AppointmentStatus.Completed, "Routine Cardiovascular Checkup", "Patient reported mild palpitation last week."),
                    createAppointment(p2, d2, today, LocalTime.of(10, 30), AppointmentStatus.InConsultation, "Severe Migraine & Dizziness", "Review MRI scan results."),
                    createAppointment(p3, d3, today, LocalTime.of(11, 45), AppointmentStatus.CheckedIn, "Knee Joint Pain Consultation", "Patient waiting in room 204."),
                    createAppointment(p4, d4, today, LocalTime.of(14, 0), AppointmentStatus.Confirmed, "Pediatric Annual Checkup", "Routine vaccination schedule."),
                    createAppointment(p5, d1, today.plusDays(1), LocalTime.of(10, 0), AppointmentStatus.Confirmed, "Post-operative ECG Evaluation", "Check stress test baseline."),
                    createAppointment(p6, d2, today.minusDays(1), LocalTime.of(15, 30), AppointmentStatus.NoShow, "Neurological Consultation", "Patient missed scheduled appointment."),
                    createAppointment(p1, d3, today.minusDays(2), LocalTime.of(16, 0), AppointmentStatus.Cancelled, "Ankle Fracture Follow-up", "Cancelled by patient via phone call.")
                );

                appointmentRepository.saveAll(appts);
                log.info("Sample Appointments seeded successfully.");
            }
        }

        // 7. Seed Invitation Codes
        if (invitationCodeRepository.count() == 0) {
            log.info("Seeding system Invitation Codes for staff registration...");
            Map<String, Role> roleMap = new HashMap<>();
            roleRepository.findAll().forEach(r -> roleMap.put(r.getName(), r));

            List<InvitationCode> codes = List.of(
                createInvitationCode("ADMIN-2026", roleMap.get("Admin")),
                createInvitationCode("DOC-CARDIO", roleMap.get("Doctor")),
                createInvitationCode("NURSE-ICU", roleMap.get("Nurse")),
                createInvitationCode("RECEPT-MAIN", roleMap.get("Receptionist")),
                createInvitationCode("PHARMA-MAIN", roleMap.get("Pharmacist")),
                createInvitationCode("LAB-MICRO", roleMap.get("LabTechnician"))
            );

            invitationCodeRepository.saveAll(codes);

            // Expired code
            InvitationCode expired = createInvitationCode("EXPIRED-2025", roleMap.get("Doctor"));
            expired.setExpiresAt(LocalDateTime.now().minusDays(10));
            invitationCodeRepository.save(expired);

            // Used code
            InvitationCode used = createInvitationCode("USED-CODE-999", roleMap.get("Receptionist"));
            used.setUsed(true);
            used.setUsedAt(LocalDateTime.now().minusDays(1));
            invitationCodeRepository.save(used);

            log.info("Invitation Codes seeded successfully.");
        }

        // 8. Seed Sample Staff Registration Requests & Email Logs
        if (staffRegistrationRequestRepository.count() == 0) {
            log.info("Seeding sample Staff Registration Requests & Email Logs...");
            Map<String, Role> roleMap = new HashMap<>();
            roleRepository.findAll().forEach(r -> roleMap.put(r.getName(), r));
            Map<String, Department> deptMap = new HashMap<>();
            departmentRepository.findAll().forEach(d -> deptMap.put(d.getName(), d));

            User adminUser = userRepository.findByUsername("admin").orElse(null);

            StaffRegistrationRequest req1 = createStaffRequest("Dr. Sarah Jenkins", "sarah@example.com", "sjenkins", "Sarah123!", "EMP-00125",
                    deptMap.get("Cardiology"), roleMap.get("Doctor"), "DOC-CARDIO", RegistrationStatus.Pending);
            StaffRegistrationRequest req2 = createStaffRequest("Michael Thomas", "michael@example.com", "mthomas", "Michael123!", "EMP-00126",
                    deptMap.get("Neurology"), roleMap.get("Nurse"), "NURSE-ICU", RegistrationStatus.Pending);
            StaffRegistrationRequest req3 = createStaffRequest("Priya Sharma", "priya@example.com", "psharma", "Priya123!", "EMP-00127",
                    deptMap.get("Pediatrics"), roleMap.get("Receptionist"), "RECEPT-MAIN", RegistrationStatus.Pending);

            StaffRegistrationRequest req4 = createStaffRequest("Dr. David Kim", "david.kim@example.com", "dkim", "David123!", "EMP-00120",
                    deptMap.get("Cardiology"), roleMap.get("Doctor"), "DOC-CARDIO", RegistrationStatus.Approved);
            req4.setReviewedByUser(adminUser);
            req4.setReviewedByUserId(adminUser != null ? adminUser.getUserId() : null);
            req4.setReviewedAt(LocalDateTime.now().minusDays(1));

            StaffRegistrationRequest req5 = createStaffRequest("Alex Carter", "alex.carter@example.com", "acarter", "Alex123!", "EMP-00118",
                    deptMap.get("Orthopedics"), roleMap.get("Receptionist"), "RECEPT-MAIN", RegistrationStatus.Rejected);
            req5.setReviewedByUser(adminUser);
            req5.setReviewedByUserId(adminUser != null ? adminUser.getUserId() : null);
            req5.setReviewedAt(LocalDateTime.now().minusDays(1));
            req5.setRejectionReason("Employee ID could not be verified with HR credentials database.");

            staffRegistrationRequestRepository.saveAll(List.of(req1, req2, req3, req4, req5));

            // Seed initial Email Delivery Logs for requests
            List<EmailLog> logs = List.of(
                createEmailLog(req1.getStaffRegistrationRequestId(), req1.getEmail(), "CareFlow HMS - Staff Registration Request Received", "RegistrationConfirmation", EmailDeliveryStatus.Sent, null),
                createEmailLog(req2.getStaffRegistrationRequestId(), req2.getEmail(), "CareFlow HMS - Staff Registration Request Received", "RegistrationConfirmation", EmailDeliveryStatus.Sent, null),
                createEmailLog(req3.getStaffRegistrationRequestId(), req3.getEmail(), "CareFlow HMS - Staff Registration Request Received", "RegistrationConfirmation", EmailDeliveryStatus.Failed, "SMTP connection timeout to local server port 1025."),
                createEmailLog(req4.getStaffRegistrationRequestId(), req4.getEmail(), "CareFlow HMS - Staff Account Approved & Activated", "StaffApproval", EmailDeliveryStatus.Sent, null),
                createEmailLog(req5.getStaffRegistrationRequestId(), req5.getEmail(), "CareFlow HMS - Staff Registration Request Update", "StaffRejection", EmailDeliveryStatus.Sent, null)
            );

            emailLogRepository.saveAll(logs);
            log.info("Sample Staff Registration Requests & Email Logs seeded successfully.");
        }
    }

    private User createUser(String username, String email, String fullName, String password, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPasswordHash(passwordHasher.hashPassword(password));
        u.setRoleId(role.getRoleId());
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    private Doctor createDoctor(String license, String firstName, String lastName, String spec, String email, String phone, Department dept, BigDecimal fee) {
        Doctor d = new Doctor();
        d.setLicenseNumber(license);
        d.setFirstName(firstName);
        d.setLastName(lastName);
        d.setSpecialization(spec);
        d.setEmail(email);
        d.setPhone(phone);
        d.setDepartmentId(dept.getDepartmentId());
        d.setDepartment(dept);
        d.setConsultationFee(fee);
        d.setActive(true);
        return d;
    }

    private Patient createPatient(String mrn, String first, String last, LocalDate dob, Gender gender, BloodGroup bg, String phone, String email, String address, String ecName, String ecPhone) {
        Patient p = new Patient();
        p.setMedicalRecordNumber(mrn);
        p.setFirstName(first);
        p.setLastName(last);
        p.setDateOfBirth(dob);
        p.setGender(gender);
        p.setBloodGroup(bg);
        p.setPhone(phone);
        p.setEmail(email);
        p.setAddress(address);
        p.setEmergencyContactName(ecName);
        p.setEmergencyContactPhone(ecPhone);
        p.setActive(true);
        return p;
    }

    private Appointment createAppointment(Patient p, Doctor d, LocalDate date, LocalTime time, AppointmentStatus status, String reason, String notes) {
        Appointment a = new Appointment();
        a.setPatientId(p.getPatientId());
        a.setPatient(p);
        a.setDoctorId(d.getDoctorId());
        a.setDoctor(d);
        a.setAppointmentDate(date);
        a.setTimeSlot(time);
        a.setStatus(status);
        a.setReason(reason);
        a.setNotes(notes);
        a.setActive(true);
        return a;
    }

    private InvitationCode createInvitationCode(String code, Role role) {
        InvitationCode inv = new InvitationCode();
        inv.setCode(code);
        inv.setCodeHash(passwordHasher.hashPassword(code));
        inv.setTargetRoleId(role.getRoleId());
        inv.setTargetRole(role);
        inv.setBoundHospitalId("CAREFLOW-HQ");
        inv.setExpiresAt(LocalDateTime.now().plusYears(1));
        inv.setUsed(false);
        inv.setActive(true);
        return inv;
    }

    private StaffRegistrationRequest createStaffRequest(String fullName, String email, String username, String password, String empId, Department dept, Role role, String code, RegistrationStatus status) {
        StaffRegistrationRequest s = new StaffRegistrationRequest();
        s.setFullName(fullName);
        s.setEmail(email);
        s.setUsername(username);
        s.setPasswordHash(passwordHasher.hashPassword(password));
        s.setEmployeeId(empId);
        s.setDepartmentId(dept != null ? dept.getDepartmentId() : null);
        s.setDepartment(dept);
        s.setRequestedRoleId(role.getRoleId());
        s.setRequestedRole(role);
        s.setInvitationCode(code);
        s.setStatus(status);
        s.setActive(true);
        return s;
    }

    private EmailLog createEmailLog(Integer reqId, String email, String subject, String template, EmailDeliveryStatus status, String error) {
        EmailLog e = new EmailLog();
        e.setStaffRegistrationRequestId(reqId);
        e.setRecipientEmail(email);
        e.setSubject(subject);
        e.setTemplateName(template);
        e.setStatus(status);
        e.setErrorMessage(error);
        e.setSentAt(LocalDateTime.now());
        e.setAttempts(1);
        e.setActive(true);
        return e;
    }
}
