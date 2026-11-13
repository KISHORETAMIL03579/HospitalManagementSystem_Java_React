package com.careflow.hospitalmanagement.common;

import com.careflow.hospitalmanagement.domain.enums.UserRole;
import java.util.*;

public final class RolePermissions {

    private RolePermissions() {}

    public static final class Permissions {
        private Permissions() {}

        // User & System Administration
        public static final String ManageUsers = "Permissions.ManageUsers";
        public static final String ManageSystem = "Permissions.ManageSystem";
        public static final String UserView = "User.View";
        public static final String UserCreate = "User.Create";
        public static final String UserUpdate = "User.Update";
        public static final String UserDeactivate = "User.Deactivate";
        public static final String UserAssignRole = "User.AssignRole";
        public static final String AuditView = "Audit.View";
        public static final String AuditExport = "Audit.Export";

        // Patient Directory & Profile
        public static final String ViewPatient = "Patient.View";
        public static final String RegisterPatient = "Patient.Create";
        public static final String UpdatePatient = "Patient.Update";
        public static final String DeactivatePatient = "Patient.Deactivate";
        public static final String SearchPatient = "Patient.Search";
        public static final String ViewOwnProfile = "Patient.ViewOwn";

        // Clinical, EHR & Encounters
        public static final String ViewEncounter = "Encounter.View";
        public static final String CreateEncounter = "Encounter.Create";
        public static final String UpdateEncounter = "Encounter.Update";
        public static final String SignEncounter = "Encounter.Sign";

        public static final String ViewDiagnosis = "Diagnosis.View";
        public static final String CreateDiagnosis = "Diagnosis.Create";
        public static final String UpdateDiagnosis = "Diagnosis.Update";

        public static final String ViewPrescription = "Prescription.View";
        public static final String CreatePrescription = "Prescription.Create";
        public static final String UpdatePrescription = "Prescription.Update";
        public static final String CancelPrescription = "Prescription.Cancel";
        public static final String SignPrescription = "Prescription.Sign";
        public static final String DispenseMedication = "Prescription.Dispense";

        public static final String RecordVitals = "Clinical.RecordVitals";

        // Laboratory
        public static final String CreateLabOrder = "LabOrder.Create";
        public static final String ViewLabOrder = "LabOrder.View";
        public static final String ViewLabResult = "LabResult.View";
        public static final String EnterLabResult = "LabResult.Enter";
        public static final String VerifyLabResult = "LabResult.Verify";
        public static final String ViewOwnLabResults = "LabResult.ViewOwn";

        // Appointments & Scheduling
        public static final String ViewAppointment = "Appointment.View";
        public static final String CreateAppointment = "Appointment.Create";
        public static final String UpdateAppointment = "Appointment.Update";
        public static final String CancelAppointment = "Appointment.Cancel";
        public static final String CheckInPatient = "Appointment.CheckIn";
        public static final String RescheduleAppointment = "Appointment.Reschedule";
        public static final String ViewOwnAppointments = "Appointment.ViewOwn";

        // Billing & Finance
        public static final String ViewInvoice = "Invoice.View";
        public static final String CreateInvoice = "Invoice.Create";
        public static final String CreatePayment = "Payment.Create";
        public static final String ViewPayment = "Payment.View";
    }

    private static final Map<UserRole, List<String>> MAP = new EnumMap<>(UserRole.class);

    static {
        MAP.put(UserRole.Admin, List.of(
            Permissions.ManageUsers,
            Permissions.ManageSystem,
            Permissions.UserView,
            Permissions.UserCreate,
            Permissions.UserUpdate,
            Permissions.UserDeactivate,
            Permissions.UserAssignRole,
            Permissions.AuditView,
            Permissions.AuditExport,
            Permissions.ViewPatient,
            Permissions.RegisterPatient,
            Permissions.UpdatePatient,
            Permissions.DeactivatePatient,
            Permissions.SearchPatient,
            Permissions.CreateAppointment,
            Permissions.ViewAppointment,
            Permissions.UpdateAppointment,
            Permissions.CancelAppointment,
            Permissions.CheckInPatient,
            Permissions.RescheduleAppointment,
            Permissions.CreateEncounter,
            Permissions.ViewEncounter,
            Permissions.CreatePrescription,
            Permissions.RecordVitals,
            Permissions.ViewLabResult,
            Permissions.ViewInvoice,
            Permissions.CreateInvoice
        ));

        MAP.put(UserRole.Doctor, List.of(
            Permissions.ViewPatient,
            Permissions.SearchPatient,
            Permissions.ViewEncounter,
            Permissions.CreateEncounter,
            Permissions.UpdateEncounter,
            Permissions.SignEncounter,
            Permissions.ViewDiagnosis,
            Permissions.CreateDiagnosis,
            Permissions.UpdateDiagnosis,
            Permissions.ViewPrescription,
            Permissions.CreatePrescription,
            Permissions.UpdatePrescription,
            Permissions.SignPrescription,
            Permissions.CreateLabOrder,
            Permissions.ViewLabOrder,
            Permissions.ViewLabResult,
            Permissions.VerifyLabResult,
            Permissions.ViewAppointment,
            Permissions.RecordVitals
        ));

        MAP.put(UserRole.Nurse, List.of(
            Permissions.ViewPatient,
            Permissions.SearchPatient,
            Permissions.RecordVitals,
            Permissions.ViewEncounter,
            Permissions.ViewAppointment,
            Permissions.CheckInPatient,
            Permissions.ViewPrescription,
            Permissions.ViewLabResult
        ));

        MAP.put(UserRole.Receptionist, List.of(
            Permissions.RegisterPatient,
            Permissions.ViewPatient,
            Permissions.UpdatePatient,
            Permissions.SearchPatient,
            Permissions.CreateAppointment,
            Permissions.ViewAppointment,
            Permissions.UpdateAppointment,
            Permissions.CancelAppointment,
            Permissions.CheckInPatient,
            Permissions.RescheduleAppointment,
            Permissions.ViewInvoice,
            Permissions.CreatePayment
        ));

        MAP.put(UserRole.HospitalManager, List.of(
            Permissions.ManageUsers,
            Permissions.UserView,
            Permissions.UserCreate,
            Permissions.UserUpdate,
            Permissions.UserDeactivate,
            Permissions.UserAssignRole,
            Permissions.AuditView,
            Permissions.ViewPatient,
            Permissions.RegisterPatient,
            Permissions.UpdatePatient,
            Permissions.SearchPatient,
            Permissions.CreateAppointment,
            Permissions.ViewAppointment,
            Permissions.UpdateAppointment,
            Permissions.CancelAppointment,
            Permissions.CheckInPatient,
            Permissions.RescheduleAppointment,
            Permissions.ViewEncounter,
            Permissions.ViewLabResult,
            Permissions.ViewInvoice
        ));

        MAP.put(UserRole.Pharmacist, List.of(
            Permissions.ViewPatient,
            Permissions.ViewPrescription,
            Permissions.DispenseMedication,
            Permissions.ViewLabResult
        ));

        MAP.put(UserRole.LabTechnician, List.of(
            Permissions.ViewPatient,
            Permissions.ViewLabOrder,
            Permissions.ViewLabResult,
            Permissions.EnterLabResult,
            Permissions.VerifyLabResult
        ));

        MAP.put(UserRole.Patient, List.of(
            Permissions.ViewOwnProfile,
            Permissions.ViewOwnAppointments,
            Permissions.ViewOwnLabResults,
            Permissions.ViewPrescription
        ));
    }

    public static List<String> getPermissions(UserRole role) {
        return MAP.getOrDefault(role, Collections.emptyList());
    }

    public static int getHierarchyLevel(UserRole role) {
        return role != null ? role.getLevel() : 0;
    }
}
