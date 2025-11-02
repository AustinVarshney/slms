package com.java.slms.serviceImpl;

import com.java.slms.dto.PromotionAssignmentRequest;
import com.java.slms.dto.StudentPromotionDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.StudentPromotionService;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;
import com.java.slms.util.PromotionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentPromotionServiceImpl implements StudentPromotionService {

    private final StudentPromotionRepository promotionRepository;
    private final StudentRepository studentRepository;
    private final ClassEntityRepository classRepository;
    private final SessionRepository sessionRepository;
    private final TeacherRepository teacherRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final SchoolRepository schoolRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final FeeRepository feeRepository;

    @Override
    @Transactional
    public StudentPromotionDto assignPromotion(Teacher teacher, PromotionAssignmentRequest request, Long schoolId) {
        log.info("Assigning promotion for student: {} by teacher: {}", request.getStudentPan(), teacher.getId());

        // Fetch student
        Student student = studentRepository.findByPanNumberIgnoreCaseAndSchoolId(request.getStudentPan(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Get active session
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        // Get student's current class
        ClassEntity currentClass = student.getCurrentClass();
        if (currentClass == null) {
            throw new WrongArgumentException("Student does not have a current class assigned");
        }

        // Verify teacher is class teacher of this class
        if (!currentClass.getClassTeacher().getId().equals(teacher.getId())) {
            throw new WrongArgumentException("You are not the class teacher of this student's class");
        }

        // Check if promotion already exists for this student and session
        Optional<StudentPromotion> existingPromotion = promotionRepository.findByStudentAndSession(
                request.getStudentPan(), activeSession.getId(), schoolId
        );

        StudentPromotion promotion;
        if (existingPromotion.isPresent()) {
            // Update existing promotion
            promotion = existingPromotion.get();
        } else {
            // Create new promotion
            promotion = new StudentPromotion();
            promotion.setStudentPan(request.getStudentPan());
            promotion.setFromClass(currentClass);
            promotion.setFromSession(activeSession);
            promotion.setAssignedByTeacher(teacher);
            promotion.setSchoolId(schoolId);
            promotion.setStatus(PromotionStatus.PENDING);
        }

        // Set target class, detention, or graduation
        if (request.getIsGraduated() != null && request.getIsGraduated()) {
            promotion.setToClass(null);
            promotion.setIsGraduated(true);
            promotion.setRemarks("Graduated");
        } else if (request.getIsDetained() != null && request.getIsDetained()) {
            // Student is DETAINED - stays in same class but moves to new session
            promotion.setToClass(currentClass); // Same class
            promotion.setIsGraduated(false);
            if (promotion.getRemarks() == null || promotion.getRemarks().isEmpty()) {
                promotion.setRemarks("Detained in " + currentClass.getClassName());
            }
            log.info("Student {} detained in class {}", request.getStudentPan(), currentClass.getClassName());
        } else if (request.getToClassId() != null) {
            ClassEntity toClass = classRepository.findByIdAndSchoolId(request.getToClassId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Target class not found"));
            promotion.setToClass(toClass);
            promotion.setIsGraduated(false);
        } else {
            // Default: if no toClass, detention, or graduation specified, treat as detained
            promotion.setToClass(currentClass);
            promotion.setIsGraduated(false);
        }

        if (request.getRemarks() != null && !request.getRemarks().isEmpty()) {
            promotion.setRemarks(request.getRemarks());
        }

        promotion = promotionRepository.save(promotion);
        log.info("Promotion assigned successfully with ID: {}", promotion.getId());

        return convertToDto(promotion);
    }

    @Override
    public List<StudentPromotionDto> getPromotionsByClass(Long classId, Long sessionId, Long schoolId) {
        log.info("Fetching promotions for class: {}, session: {}", classId, sessionId);
        List<StudentPromotion> promotions = promotionRepository.findByClassAndSession(classId, sessionId, schoolId);
        return promotions.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public StudentPromotionDto getPromotionByStudent(String studentPan, Long sessionId, Long schoolId) {
        log.info("Fetching promotion for student: {}", studentPan);
        Optional<StudentPromotion> promotion = promotionRepository.findByStudentAndSession(studentPan, sessionId, schoolId);
        return promotion.map(this::convertToDto).orElse(null);
    }

    @Override
    @Transactional
    public void executePromotions(Long fromSessionId, Long toSessionId, Long schoolId) {
        log.info("Executing promotions from session: {} to session: {}", fromSessionId, toSessionId);

        Session fromSession = sessionRepository.findById(fromSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Source session not found"));
                
        Session toSession = sessionRepository.findById(toSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Target session not found"));

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        List<StudentPromotion> pendingPromotions = promotionRepository.findBySessionAndStatus(
                fromSessionId, schoolId, PromotionStatus.PENDING
        );

        if (pendingPromotions.isEmpty()) {
            log.info("No pending promotions found for session {} in school {}", fromSessionId, schoolId);
            return;
        }

        for (StudentPromotion promotion : pendingPromotions) {
            try {
                Student student = studentRepository.findByPanNumberIgnoreCaseAndSchoolId(promotion.getStudentPan(), schoolId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + promotion.getStudentPan()));

                if (promotion.getIsGraduated()) {
                    // Mark student as graduated and clear class assignment
                    student.setStatus(com.java.slms.util.UserStatus.GRADUATED);
                    student.setCurrentClass(null); // Clear class - graduated students don't belong to any class
                    student.setClassRollNumber(null); // Clear roll number
                    // Keep session reference for record keeping
                    promotion.setStatus(PromotionStatus.GRADUATED);
                    log.info("Student {} marked as GRADUATED - removed from class", student.getPanNumber());
                } else if (promotion.getToClass() != null) {
                    // Find the corresponding class in the new session
                    // The toClass from promotion is in the old session, we need to find it by name in new session
                    String targetClassName = promotion.getToClass().getClassName();
                    ClassEntity newSessionClass = classRepository.findByClassNameIgnoreCaseAndSessionIdAndSchoolId(
                            targetClassName, toSessionId, schoolId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Class " + targetClassName + " not found in new session. " +
                                    "Please ensure all classes are created in the new session before executing promotions."));

                    // Check if enrollment already exists for this student in the new session
                    boolean enrollmentExists = studentEnrollmentRepository
                            .existsByStudent_PanNumberAndSession_IdAndClassEntity_Id(
                                    student.getPanNumber(), toSession.getId(), newSessionClass.getId());

                    // Check if student is already in the new session (avoid updating if already promoted)
                    boolean alreadyPromoted = student.getSession() != null && 
                                            student.getSession().getId() != null &&
                                            toSession.getId() != null &&
                                            student.getSession().getId().equals(toSession.getId()) &&
                                            student.getCurrentClass() != null &&
                                            student.getCurrentClass().getId() != null &&
                                            newSessionClass.getId() != null &&
                                            student.getCurrentClass().getId().equals(newSessionClass.getId());
                    
                    if (alreadyPromoted) {
                        log.info("Student {} already promoted to class {} in session {} - skipping update", 
                                student.getPanNumber(), newSessionClass.getClassName(), toSession.getName());
                    } else {
                        log.info("Promoting student {} from session {} to session {}", 
                                student.getPanNumber(), 
                                student.getSession() != null ? student.getSession().getName() : "null",
                                toSession.getName());
                        
                        // Clear the class roll number to avoid unique constraint violation
                        // Roll number will be reassigned by admin in the new session
                        Integer oldRollNumber = student.getClassRollNumber();
                        student.setClassRollNumber(null);
                        
                        // Promote student to next class in new session
                        student.setCurrentClass(newSessionClass);
                        student.setSession(toSession);
                        log.info("Student {} promoted: roll number {} -> null, class updated to {}, session updated to {}", 
                                student.getPanNumber(), oldRollNumber, newSessionClass.getClassName(), toSession.getName());
                    }
                    
                    promotion.setToSession(toSession);
                    promotion.setStatus(PromotionStatus.PROMOTED);
                    
                    // Create enrollment for new session only if it doesn't already exist
                    if (!enrollmentExists) {
                        StudentEnrollments enrollment = new StudentEnrollments();
                        enrollment.setStudent(student);
                        enrollment.setSchool(school);
                        enrollment.setClassEntity(newSessionClass);
                        enrollment.setSession(toSession);
                        studentEnrollmentRepository.save(enrollment);
                        log.info("Created enrollment for student {} in class {} for session {}", 
                                student.getPanNumber(), newSessionClass.getClassName(), toSession.getName());
                    } else {
                        log.info("Enrollment already exists for student {} in new session", student.getPanNumber());
                    }
                    
                    // Generate fees for new session only if not already promoted
                    if (!alreadyPromoted) {
                        generateFeesForNewSession(student, newSessionClass, toSession, school);
                    }
                    
                    log.info("Student {} promoted to class {} in session {}", 
                            student.getPanNumber(), 
                            newSessionClass.getClassName(), 
                            toSession.getName());
                } else {
                    // If toClass is null and not graduated, student is DETAINED (remains in same class)
                    // Find the same class name in the new session
                    String currentClassName = student.getCurrentClass().getClassName();
                    ClassEntity newSessionClass = classRepository.findByClassNameIgnoreCaseAndSessionIdAndSchoolId(
                            currentClassName, toSessionId, schoolId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Class " + currentClassName + " not found in new session for detained student"));
                    
                    // Update student to new session but keep same class level
                    student.setCurrentClass(newSessionClass);
                    student.setSession(toSession);
                    student.setClassRollNumber(null); // Reset roll number
                    
                    promotion.setStatus(PromotionStatus.DETAINED);
                    promotion.setToSession(toSession);
                    promotion.setToClass(newSessionClass); // Keep track of which class they stayed in
                    
                    // Create enrollment for detained student in new session
                    boolean enrollmentExists = studentEnrollmentRepository
                            .existsByStudent_PanNumberAndSession_IdAndClassEntity_Id(
                                    student.getPanNumber(), toSession.getId(), newSessionClass.getId());
                    
                    if (!enrollmentExists) {
                        StudentEnrollments enrollment = new StudentEnrollments();
                        enrollment.setStudent(student);
                        enrollment.setSchool(school);
                        enrollment.setClassEntity(newSessionClass);
                        enrollment.setSession(toSession);
                        studentEnrollmentRepository.save(enrollment);
                        log.info("Created enrollment for detained student {} in class {}", 
                                student.getPanNumber(), newSessionClass.getClassName());
                    }
                    
                    // Generate fees for detained student in new session
                    generateFeesForNewSession(student, newSessionClass, toSession, school);
                    
                    log.info("Student {} detained in class {} - moved to new session {}", 
                            student.getPanNumber(), 
                            newSessionClass.getClassName(),
                            toSession.getName());
                }

                studentRepository.save(student);
                promotionRepository.save(promotion);

            } catch (Exception e) {
                log.error("Error promoting student: {}", promotion.getStudentPan(), e);
            }
        }

        log.info("Promotions executed successfully. Total processed: {}", pendingPromotions.size());
        
        // After processing all promotions, handle students who don't have promotion records
        // These students should automatically move to the same class in the new session
        handleStudentsWithoutPromotions(fromSession, toSession, school);
    }

    @Override
    @Transactional
    public void deletePromotion(Long promotionId, Long schoolId) {
        log.info("Deleting promotion: {}", promotionId);
        StudentPromotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        if (!promotion.getSchoolId().equals(schoolId)) {
            throw new WrongArgumentException("Promotion does not belong to this school");
        }

        if (promotion.getStatus() != PromotionStatus.PENDING) {
            throw new WrongArgumentException("Cannot delete a promotion that has been executed");
        }

        promotionRepository.delete(promotion);
        log.info("Promotion deleted successfully");
    }

    @Override
    public List<StudentPromotionDto> getPendingPromotions(Long sessionId, Long schoolId) {
        log.info("Fetching pending promotions for session: {}", sessionId);
        List<StudentPromotion> promotions = promotionRepository.findBySessionAndStatus(
                sessionId, schoolId, PromotionStatus.PENDING
        );
        return promotions.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<StudentPromotionDto> getPromotionsBySession(Long sessionId, Long schoolId) {
        log.info("Fetching all promotions for session: {}", sessionId);
        List<StudentPromotion> promotions = promotionRepository.findBySchoolAndSession(schoolId, sessionId);
        return promotions.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private StudentPromotionDto convertToDto(StudentPromotion promotion) {
        StudentPromotionDto dto = new StudentPromotionDto();
        dto.setId(promotion.getId());
        dto.setStudentPan(promotion.getStudentPan());

        // Get student name
        Optional<Student> studentOpt = studentRepository.findByPanNumberIgnoreCaseAndSchoolId(
                promotion.getStudentPan(), promotion.getSchoolId()
        );
        studentOpt.ifPresent(student -> dto.setStudentName(student.getName()));

        dto.setFromClassId(promotion.getFromClass().getId());
        dto.setFromClassName(promotion.getFromClass().getClassName());
        dto.setFromSessionId(promotion.getFromSession().getId());
        dto.setFromSessionName(promotion.getFromSession().getName());

        if (promotion.getToClass() != null) {
            dto.setToClassId(promotion.getToClass().getId());
            dto.setToClassName(promotion.getToClass().getClassName());
        }

        if (promotion.getToSession() != null) {
            dto.setToSessionId(promotion.getToSession().getId());
            dto.setToSessionName(promotion.getToSession().getName());
        }

        if (promotion.getAssignedByTeacher() != null) {
            dto.setAssignedByTeacherId(promotion.getAssignedByTeacher().getId());
            dto.setAssignedByTeacherName(promotion.getAssignedByTeacher().getName());
        }

        dto.setStatus(promotion.getStatus());
        dto.setRemarks(promotion.getRemarks());
        dto.setIsGraduated(promotion.getIsGraduated());
        
        // Check if student is detained (same class as from class)
        if (promotion.getToClass() != null && promotion.getFromClass() != null) {
            dto.setIsDetained(promotion.getToClass().getId().equals(promotion.getFromClass().getId()));
        } else {
            dto.setIsDetained(false);
        }
        
        dto.setSchoolId(promotion.getSchoolId());

        return dto;
    }

    /**
     * Generate fees for student in new session based on fee structure
     */
    private void generateFeesForNewSession(Student student, ClassEntity classEntity, Session session, School school) {
        try {
            log.info("Generating fees for student {} in class {} for session {}", 
                    student.getPanNumber(), classEntity.getClassName(), session.getName());
            
            // Find fee structure for the class in the new session
            Optional<FeeStructure> feeStructureOpt = feeStructureRepository
                    .findByClassEntity_IdAndSession_IdAndSchool_Id(
                            classEntity.getId(), 
                            session.getId(), 
                            school.getId()
                    );
            
            if (feeStructureOpt.isEmpty()) {
                log.warn("No fee structure found for class {} in session {}. Skipping fee generation for student {}",
                        classEntity.getClassName(), session.getName(), student.getPanNumber());
                return;
            }
            
            FeeStructure feeStructure = feeStructureOpt.get();
            
            // Get session start and end dates to determine which months to generate fees for
            LocalDate sessionStart = session.getStartDate();
            LocalDate sessionEnd = session.getEndDate();
            
            // Get starting month from session start date
            Month startMonth = sessionStart.getMonth();
            int startYear = sessionStart.getYear();
            
            // Generate fees for 12 consecutive months starting from session start month
            LocalDate currentMonth = sessionStart.withDayOfMonth(1); // Start from 1st of start month
            int feesCreated = 0;
            
            for (int i = 0; i < 12; i++) {
                // Convert Month to FeeMonth enum
                FeeMonth feeMonth = FeeMonth.valueOf(currentMonth.getMonth().name());
                int year = currentMonth.getYear();
                
                // Check if fee already exists for this month/year/session
                boolean exists = feeRepository.existsByStudentPanNumberAndMonthAndYearAndSession(
                        student.getPanNumber(), feeMonth, year, session);
                
                if (!exists) {
                    Fee fee = new Fee();
                    fee.setStudent(student);
                    fee.setFeeStructure(feeStructure);
                    fee.setClassEntity(classEntity);
                    fee.setMonth(feeMonth);
                    fee.setYear(year);
                    fee.setAmount(feeStructure.getFeesAmount());
                    fee.setStatus(FeeStatus.PENDING);
                    fee.setSchool(school);
                    fee.setSession(session);
                    
                    // Calculate due date (10th of each month)
                    fee.setDueDate(LocalDate.of(year, currentMonth.getMonth(), 10));
                    
                    feeRepository.save(fee);
                    feesCreated++;
                }
                
                // Move to next month
                currentMonth = currentMonth.plusMonths(1);
            }
            
            log.info("Successfully generated {} fee records for student {} in session {} starting from {}", 
                    feesCreated, student.getPanNumber(), session.getName(), startMonth);
                    
                    
        } catch (Exception e) {
            log.error("Error generating fees for student {}: {}", student.getPanNumber(), e.getMessage(), e);
            // Don't throw exception - promotion should continue even if fee generation fails
        }
    }
    
    /**
     * Handle students who don't have promotion assignments
     * These students should automatically move to the same class in the new session
     */
    private void handleStudentsWithoutPromotions(Session fromSession, Session toSession, School school) {
        log.info("Handling students without promotion assignments from session {} to {}", 
                fromSession.getName(), toSession.getName());
        
        // Get all ACTIVE students in the old session
        List<Student> studentsInOldSession = studentRepository.findBySession_IdAndSchool_IdAndStatus(
                fromSession.getId(), school.getId(), com.java.slms.util.UserStatus.ACTIVE);
        
        // Get all students who have promotion records
        List<StudentPromotion> allPromotions = promotionRepository.findBySchoolAndSession(
                school.getId(), fromSession.getId());
        Set<String> studentsWithPromotions = allPromotions.stream()
                .map(StudentPromotion::getStudentPan)
                .collect(Collectors.toSet());
        
        // Filter students who don't have promotions
        List<Student> studentsWithoutPromotions = studentsInOldSession.stream()
                .filter(student -> !studentsWithPromotions.contains(student.getPanNumber()))
                .collect(Collectors.toList());
        
        if (studentsWithoutPromotions.isEmpty()) {
            log.info("All students have promotion assignments");
            return;
        }
        
        log.info("Found {} students without promotion assignments - moving them to same class in new session", 
                studentsWithoutPromotions.size());
        
        for (Student student : studentsWithoutPromotions) {
            try {
                // Find the same class in the new session
                String currentClassName = student.getCurrentClass().getClassName();
                Optional<ClassEntity> newSessionClassOpt = classRepository.findByClassNameIgnoreCaseAndSessionIdAndSchoolId(
                        currentClassName, toSession.getId(), school.getId());
                
                if (newSessionClassOpt.isEmpty()) {
                    log.warn("Class {} not found in new session for student {} - skipping", 
                            currentClassName, student.getPanNumber());
                    continue;
                }
                
                ClassEntity newSessionClass = newSessionClassOpt.get();
                
                // Update student to new session
                student.setCurrentClass(newSessionClass);
                student.setSession(toSession);
                student.setClassRollNumber(null); // Reset roll number
                
                // Create enrollment
                boolean enrollmentExists = studentEnrollmentRepository
                        .existsByStudent_PanNumberAndSession_IdAndClassEntity_Id(
                                student.getPanNumber(), toSession.getId(), newSessionClass.getId());
                
                if (!enrollmentExists) {
                    StudentEnrollments enrollment = new StudentEnrollments();
                    enrollment.setStudent(student);
                    enrollment.setSchool(school);
                    enrollment.setClassEntity(newSessionClass);
                    enrollment.setSession(toSession);
                    studentEnrollmentRepository.save(enrollment);
                }
                
                // Generate fees for the new session
                generateFeesForNewSession(student, newSessionClass, toSession, school);
                
                studentRepository.save(student);
                
                log.info("Student {} automatically moved to class {} in new session {}", 
                        student.getPanNumber(), newSessionClass.getClassName(), toSession.getName());
                
            } catch (Exception e) {
                log.error("Error handling student without promotion: {}", student.getPanNumber(), e);
            }
        }
        
        log.info("Completed handling {} students without promotions", studentsWithoutPromotions.size());
    }
}

