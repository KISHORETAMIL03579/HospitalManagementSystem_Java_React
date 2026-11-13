package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.*;
import com.careflow.hospitalmanagement.domain.enums.AppointmentStatus;
import com.careflow.hospitalmanagement.domain.enums.ScheduleExceptionType;
import com.careflow.hospitalmanagement.dto.doctor.*;
import com.careflow.hospitalmanagement.exception.ResourceNotFoundException;
import com.careflow.hospitalmanagement.repository.AppointmentRepository;
import com.careflow.hospitalmanagement.repository.DoctorRepository;
import com.careflow.hospitalmanagement.repository.DoctorScheduleExceptionRepository;
import com.careflow.hospitalmanagement.repository.DoctorScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class DoctorAvailabilityService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorScheduleExceptionRepository exceptionRepository;
    private final AppointmentRepository appointmentRepository;
    private Clock clock = Clock.systemDefaultZone();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    public DoctorAvailabilityService(
            DoctorRepository doctorRepository,
            DoctorScheduleRepository scheduleRepository,
            DoctorScheduleExceptionRepository exceptionRepository,
            AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.exceptionRepository = exceptionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Autowired(required = false)
    public void setClock(Clock clock) {
        if (clock != null) {
            this.clock = clock;
        }
    }

    @Transactional(readOnly = true)
    public DoctorAvailabilityDto getDoctorAvailability(int doctorId, LocalDate date) {
        return getDoctorAvailability(doctorId, date, this.clock);
    }

    @Transactional(readOnly = true)
    public DoctorAvailabilityDto getDoctorAvailability(int doctorId, LocalDate date, Clock evalClock) {
        Doctor doctor = doctorRepository.findByIdWithDepartment(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with ID " + doctorId + " was not found."));

        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndIsActiveTrueOrderByStartTimeAsc(doctorId);
        List<DoctorScheduleException> exceptions = exceptionRepository.findByDoctorIdAndDate(doctorId, date);
        List<Appointment> appointments = appointmentRepository.findFiltered(null, doctorId, date).stream()
                .filter(a -> a.isActive() && a.getStatus() != AppointmentStatus.Cancelled && a.getStatus() != AppointmentStatus.NoShow)
                .toList();

        LocalDate today = LocalDate.now(evalClock);
        LocalTime currentTime = LocalTime.now(evalClock);

        DoctorAvailabilityDto result = new DoctorAvailabilityDto();
        result.setDoctorId(doctor.getDoctorId());
        result.setDoctorName(doctor.getFullName());
        result.setSpecialization(doctor.getSpecialization());
        result.setDepartmentName(doctor.getDepartment() != null ? doctor.getDepartment().getName() : "General Medicine");
        result.setConsultationFee(doctor.getConsultationFee());
        result.setDate(date);
        result.setTimeZone(evalClock.getZone().getId());

        if (!doctor.isActive()) {
            result.setAvailabilityStatus("Inactive");
            result.setStatusMessage("Doctor account is currently inactive.");
            return result;
        }

        // Layer 1: Base recurring schedules for day of week
        List<DoctorSchedule> daySchedules = schedules.stream()
                .filter(s -> s.getDayOfWeek() == date.getDayOfWeek())
                .sorted(Comparator.comparing(DoctorSchedule::getStartTime))
                .toList();

        List<TimeSlotDto> slots = new ArrayList<>();
        for (DoctorSchedule sched : daySchedules) {
            int duration = sched.getSlotDurationMinutes() > 0 ? sched.getSlotDurationMinutes() : 30;
            LocalTime current = sched.getStartTime();

            while (!current.plusMinutes(duration).isAfter(sched.getEndTime())) {
                LocalTime slotEnd = current.plusMinutes(duration);
                TimeSlotDto slot = new TimeSlotDto();
                slot.setStartTime(current);
                slot.setEndTime(slotEnd);
                slot.setFormattedTime(formatTime(current) + " - " + formatTime(slotEnd));
                slot.setBookable(true);
                slot.setStatus("Available");
                slot.setSessionName(sched.getSessionName() != null ? sched.getSessionName() : "Regular Session");
                slots.add(slot);

                current = slotEnd;
            }
        }

        // Layer 2: Extra working hours exceptions
        List<DoctorScheduleException> extraHours = exceptions.stream()
                .filter(e -> e.getExceptionType() == ScheduleExceptionType.ExtraWorkingHours && e.getStartTime() != null && e.getEndTime() != null)
                .toList();

        for (DoctorScheduleException extra : extraHours) {
            LocalTime start = extra.getStartTime();
            LocalTime end = extra.getEndTime();
            int duration = 30;
            LocalTime current = start;

            while (!current.plusMinutes(duration).isAfter(end)) {
                LocalTime slotEnd = current.plusMinutes(duration);
                final LocalTime matchStart = current;
                if (slots.stream().noneMatch(s -> s.getStartTime().equals(matchStart))) {
                    TimeSlotDto slot = new TimeSlotDto();
                    slot.setStartTime(current);
                    slot.setEndTime(slotEnd);
                    slot.setFormattedTime(formatTime(current) + " - " + formatTime(slotEnd));
                    slot.setBookable(true);
                    slot.setStatus("Available");
                    slot.setSessionName(extra.getReason() != null && !extra.getReason().isBlank()
                            ? "Extra Hours (" + extra.getReason() + ")"
                            : "Extra Working Hours");
                    slots.add(slot);
                }
                current = slotEnd;
            }
        }

        // Layer 3: Check for FullDayLeave
        Optional<DoctorScheduleException> fullDayLeave = exceptions.stream()
                .filter(e -> e.getExceptionType() == ScheduleExceptionType.FullDayLeave)
                .findFirst();

        if (fullDayLeave.isPresent()) {
            String leaveReason = fullDayLeave.get().getReason() != null && !fullDayLeave.get().getReason().isBlank()
                    ? fullDayLeave.get().getReason()
                    : "On Leave";
            for (TimeSlotDto slot : slots) {
                slot.setBookable(false);
                slot.setStatus("Blocked");
                slot.setBlockReason(leaveReason);
            }
            slots.sort(Comparator.comparing(TimeSlotDto::getStartTime));
            result.setSlots(slots);
            result.setTotalSlotsCount(slots.size());
            result.setBookableSlotsCount(0);
            result.setBlockedSlotsCount(slots.size());
            result.setAvailabilityStatus("On leave");
            result.setStatusMessage("On leave: " + leaveReason);
            return result;
        }

        // Layer 4: Partial unavailable blocks
        List<DoctorScheduleException> unavailableBlocks = exceptions.stream()
                .filter(e -> e.getExceptionType() == ScheduleExceptionType.Unavailable && e.getStartTime() != null && e.getEndTime() != null)
                .toList();

        for (DoctorScheduleException block : unavailableBlocks) {
            LocalTime blockStart = block.getStartTime();
            LocalTime blockEnd = block.getEndTime();
            String reason = block.getReason() != null && !block.getReason().isBlank() ? block.getReason() : "Unavailable";

            for (TimeSlotDto slot : slots) {
                if (slot.getStartTime().isBefore(blockEnd) && blockStart.isBefore(slot.getEndTime())) {
                    slot.setBookable(false);
                    slot.setStatus("Blocked");
                    slot.setBlockReason(reason);
                }
            }
        }

        // Layer 5: Active appointments
        for (Appointment appt : appointments) {
            LocalTime apptStart = appt.getTimeSlot();
            LocalTime apptEnd = apptStart.plusMinutes(30);

            for (TimeSlotDto slot : slots) {
                if (slot.getStartTime().isBefore(apptEnd) && apptStart.isBefore(slot.getEndTime())) {
                    slot.setBookable(false);
                    slot.setStatus("Booked");
                }
            }
        }

        // Layer 6: Past slots
        if (date.isEqual(today)) {
            for (TimeSlotDto slot : slots) {
                if (!slot.getStartTime().isAfter(currentTime) && slot.isBookable()) {
                    slot.setBookable(false);
                    slot.setStatus("Past");
                }
            }
        } else if (date.isBefore(today)) {
            for (TimeSlotDto slot : slots) {
                slot.setBookable(false);
                slot.setStatus("Past");
            }
        }

        slots.sort(Comparator.comparing(TimeSlotDto::getStartTime));
        result.setSlots(slots);
        result.setTotalSlotsCount(slots.size());

        int bookableCount = (int) slots.stream().filter(TimeSlotDto::isBookable).count();
        int bookedCount = (int) slots.stream().filter(s -> "Booked".equals(s.getStatus())).count();
        int blockedCount = (int) slots.stream().filter(s -> "Blocked".equals(s.getStatus())).count();

        result.setBookableSlotsCount(bookableCount);
        result.setBookedSlotsCount(bookedCount);
        result.setBlockedSlotsCount(blockedCount);

        TimeSlotDto firstBookable = slots.stream().filter(TimeSlotDto::isBookable).findFirst().orElse(null);
        if (firstBookable != null) {
            result.setNextAvailableSlot(firstBookable.getStartTime());
        }

        // Determine dynamic availability status
        if (slots.isEmpty()) {
            result.setAvailabilityStatus("Unavailable");
            result.setStatusMessage("No scheduled consultation hours for this day.");
        } else if (bookableCount == 0) {
            result.setAvailabilityStatus("Fully booked");
            result.setStatusMessage("All consultation slots for this date are fully booked or unavailable.");
        } else if (date.isEqual(today)) {
            boolean isCurrentSlotAvailable = slots.stream()
                    .anyMatch(s -> s.isBookable() && !s.getStartTime().isAfter(currentTime) && currentTime.isBefore(s.getEndTime()));
            if (isCurrentSlotAvailable) {
                result.setAvailabilityStatus("Available now");
                result.setStatusMessage("Available now (Next slot: " + formatTime(firstBookable.getStartTime()) + ")");
            } else {
                result.setAvailabilityStatus("Available today");
                result.setStatusMessage("Next slot: " + formatTime(firstBookable.getStartTime()));
            }
        } else {
            result.setAvailabilityStatus("Available later");
            result.setStatusMessage("Next slot: " + formatTime(firstBookable.getStartTime()));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<DoctorDirectoryItemDto> getDoctorDirectory(Integer departmentId, String search, LocalDate targetDate) {
        List<Doctor> doctors = doctorRepository.findAllActiveWithDepartment(departmentId);

        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            doctors = doctors.stream().filter(d ->
                d.getFullName().toLowerCase().contains(q) ||
                d.getSpecialization().toLowerCase().contains(q) ||
                (d.getDepartment() != null && d.getDepartment().getName().toLowerCase().contains(q))
            ).toList();
        }

        LocalDate today = targetDate != null ? targetDate : LocalDate.now(clock);
        List<DoctorDirectoryItemDto> list = new ArrayList<>();

        for (Doctor doctor : doctors) {
            DoctorAvailabilityDto todayAvail = getDoctorAvailability(doctor.getDoctorId(), today, clock);

            DoctorDirectoryItemDto item = new DoctorDirectoryItemDto();
            item.setDoctorId(doctor.getDoctorId());
            item.setFullName(doctor.getFullName());
            item.setSpecialization(doctor.getSpecialization());
            item.setDepartmentId(doctor.getDepartmentId());
            item.setDepartmentName(doctor.getDepartment() != null ? doctor.getDepartment().getName() : "General Medicine");
            item.setLicenseNumber(doctor.getLicenseNumber());
            item.setConsultationFee(doctor.getConsultationFee());
            item.setPhone(doctor.getPhone());
            item.setEmail(doctor.getEmail());
            item.setYearsOfExperience(8);
            item.setRating(4.9);
            item.setReviewCount(38);
            item.setLanguages("English, Spanish");
            item.setBiography(doctor.getSpecialization() + " specialist with expertise in compassionate clinical care.");
            item.setActive(doctor.isActive());
            item.setAvailabilityStatus(todayAvail.getAvailabilityStatus());
            item.setStatusBadge(todayAvail.getAvailabilityStatus());

            if (todayAvail.getBookableSlotsCount() > 0 && todayAvail.getNextAvailableSlot() != null) {
                item.setNextAvailableDate(today);
                item.setNextAvailableTime(todayAvail.getNextAvailableSlot());
                item.setNextAvailableSlotText("Available now".equals(todayAvail.getAvailabilityStatus())
                        ? "Available now · Slot: " + formatTime(todayAvail.getNextAvailableSlot())
                        : "Next slot: " + formatTime(todayAvail.getNextAvailableSlot()));
            } else {
                LocalDate foundDate = null;
                LocalTime foundTime = null;

                for (int i = 1; i <= 7; i++) {
                    LocalDate checkDate = today.plusDays(i);
                    DoctorAvailabilityDto futureAvail = getDoctorAvailability(doctor.getDoctorId(), checkDate, clock);
                    if (futureAvail.getBookableSlotsCount() > 0 && futureAvail.getNextAvailableSlot() != null) {
                        foundDate = checkDate;
                        foundTime = futureAvail.getNextAvailableSlot();
                        break;
                    }
                }

                if (foundDate != null && foundTime != null) {
                    item.setNextAvailableDate(foundDate);
                    item.setNextAvailableTime(foundTime);
                    if (foundDate.equals(today.plusDays(1))) {
                        item.setNextAvailableSlotText("Available tomorrow at " + formatTime(foundTime));
                    } else {
                        item.setNextAvailableSlotText("Available " + foundDate.getDayOfWeek().toString().substring(0, 3) + ", " +
                                foundDate.getMonth().toString().substring(0, 3) + " " + foundDate.getDayOfMonth() + " at " + formatTime(foundTime));
                    }
                } else {
                    item.setNextAvailableSlotText("On leave".equals(todayAvail.getAvailabilityStatus())
                            ? todayAvail.getStatusMessage()
                            : "No upcoming slots this week");
                }
            }

            list.add(item);
        }

        return list;
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleDto> getDoctorSchedules(int doctorId) {
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndIsActiveTrueOrderByStartTimeAsc(doctorId);
        List<DoctorScheduleDto> dtos = new ArrayList<>();
        for (DoctorSchedule s : schedules) {
            DoctorScheduleDto dto = new DoctorScheduleDto();
            dto.setScheduleId(s.getScheduleId());
            dto.setDoctorId(s.getDoctorId());
            dto.setDayOfWeek(s.getDayOfWeek());
            dto.setStartTime(s.getStartTime());
            dto.setEndTime(s.getEndTime());
            dto.setSlotDurationMinutes(s.getSlotDurationMinutes());
            dto.setSessionName(s.getSessionName());
            dto.setActive(s.isActive());
            dtos.add(dto);
        }
        return dtos;
    }

    public List<DoctorScheduleDto> saveDoctorSchedules(int doctorId, List<SaveDoctorScheduleDto> schedules) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with ID " + doctorId + " was not found."));

        scheduleRepository.deleteByDoctorId(doctorId);

        List<DoctorSchedule> entities = new ArrayList<>();
        for (SaveDoctorScheduleDto dto : schedules) {
            DoctorSchedule s = new DoctorSchedule();
            s.setDoctorId(doctorId);
            s.setDoctor(doctor);
            s.setDayOfWeek(dto.getDayOfWeek());
            s.setStartTime(dto.getStartTime());
            s.setEndTime(dto.getEndTime());
            s.setSlotDurationMinutes(dto.getSlotDurationMinutes() > 0 ? dto.getSlotDurationMinutes() : 30);
            s.setSessionName(dto.getSessionName() != null ? dto.getSessionName() : "Clinic Session");
            s.setActive(true);
            entities.add(s);
        }

        scheduleRepository.saveAll(entities);
        return getDoctorSchedules(doctorId);
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleExceptionDto> getDoctorExceptions(int doctorId, LocalDate fromDate, LocalDate toDate) {
        List<DoctorScheduleException> list = exceptionRepository.findExceptions(doctorId, fromDate, toDate);
        List<DoctorScheduleExceptionDto> dtos = new ArrayList<>();
        for (DoctorScheduleException e : list) {
            DoctorScheduleExceptionDto dto = new DoctorScheduleExceptionDto();
            dto.setExceptionId(e.getExceptionId());
            dto.setDoctorId(e.getDoctorId());
            dto.setDate(e.getExceptionDate());
            dto.setStartTime(e.getStartTime());
            dto.setEndTime(e.getEndTime());
            dto.setExceptionType(e.getExceptionType());
            dto.setReason(e.getReason());
            dto.setCreatedAt(e.getCreatedAt());
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public ConflictCheckResultDto checkExceptionConflicts(int doctorId, CreateDoctorScheduleExceptionDto dto) {
        List<Appointment> appointments = appointmentRepository.findFiltered(null, doctorId, dto.getDate()).stream()
                .filter(a -> a.isActive() && a.getStatus() != AppointmentStatus.Cancelled && a.getStatus() != AppointmentStatus.NoShow)
                .toList();

        List<ConflictingAppointmentDto> conflicts = new ArrayList<>();

        for (Appointment appt : appointments) {
            boolean isConflict = false;
            if (dto.getExceptionType() == ScheduleExceptionType.FullDayLeave) {
                isConflict = true;
            } else if (dto.getExceptionType() == ScheduleExceptionType.Unavailable && dto.getStartTime() != null && dto.getEndTime() != null) {
                LocalTime apptStart = appt.getTimeSlot();
                LocalTime apptEnd = apptStart.plusMinutes(30);
                if (apptStart.isBefore(dto.getEndTime()) && dto.getStartTime().isBefore(apptEnd)) {
                    isConflict = true;
                }
            }

            if (isConflict) {
                ConflictingAppointmentDto c = new ConflictingAppointmentDto();
                c.setAppointmentId(appt.getAppointmentId());
                c.setPatientName(appt.getPatient() != null ? appt.getPatient().getFullName() : "Patient");
                c.setMedicalRecordNumber(appt.getPatient() != null ? appt.getPatient().getMedicalRecordNumber() : "");
                c.setAppointmentDate(appt.getAppointmentDate());
                c.setTimeSlot(appt.getTimeSlot());
                c.setStatus(appt.getStatus().name());
                c.setReason(appt.getReason() != null ? appt.getReason() : "Consultation");
                conflicts.add(c);
            }
        }

        ConflictCheckResultDto result = new ConflictCheckResultDto();
        result.setHasConflicts(!conflicts.isEmpty());
        result.setConflictCount(conflicts.size());
        result.setConflictingAppointments(conflicts);
        result.setMessage(!conflicts.isEmpty()
                ? conflicts.size() + " existing appointment(s) conflict with this schedule exception."
                : "No conflicting appointments found.");
        return result;
    }

    public DoctorScheduleExceptionDto createException(int doctorId, CreateDoctorScheduleExceptionDto dto) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with ID " + doctorId + " was not found."));

        DoctorScheduleException e = new DoctorScheduleException();
        e.setDoctorId(doctorId);
        e.setDoctor(doctor);
        e.setExceptionDate(dto.getDate());
        e.setStartTime(dto.getStartTime());
        e.setEndTime(dto.getEndTime());
        e.setExceptionType(dto.getExceptionType());
        e.setReason(dto.getReason());
        e.setActive(true);

        DoctorScheduleException created = exceptionRepository.save(e);

        DoctorScheduleExceptionDto result = new DoctorScheduleExceptionDto();
        result.setExceptionId(created.getExceptionId());
        result.setDoctorId(created.getDoctorId());
        result.setDate(created.getExceptionDate());
        result.setStartTime(created.getStartTime());
        result.setEndTime(created.getEndTime());
        result.setExceptionType(created.getExceptionType());
        result.setReason(created.getReason());
        result.setCreatedAt(created.getCreatedAt());
        return result;
    }

    public void deleteException(int doctorId, int exceptionId) {
        exceptionRepository.findById(exceptionId).ifPresent(e -> {
            if (e.getDoctorId().equals(doctorId)) {
                exceptionRepository.delete(e);
            }
        });
    }

    private static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "";
    }
}
