package com.java.slms.serviceImpl;

import com.java.slms.dto.*;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.FeeService;
import com.java.slms.service.StudentService;
import com.java.slms.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class StudentServiceImpl implements StudentService
{

    private final ModelMapper modelMapper;
    private final StudentRepository studentRepository;
    private final ClassEntityRepository classEntityRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final FeeRepository feeRepository;
    private final FeeService feeService;
    private final SchoolRepository schoolRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final ScoreRepository scoreRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public StudentResponseDto createStudent(StudentRequestDto studentRequestDto, Long schoolId)
    {
        log.info("Attempting to create student with PAN: {}", studentRequestDto.getPanNumber());

        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        if (studentRepository.findByPanNumberIgnoreCase(studentRequestDto.getPanNumber()).isPresent())
        {
            throw new AlreadyExistException("A student with PAN number '" + studentRequestDto.getPanNumber() + "' already exists.");
        }
        User user = fetchUserByPan(studentRequestDto.getPanNumber());
        ClassEntity classEntity = classEntityRepository
                .findByIdAndSchoolIdAndSessionActive(studentRequestDto.getClassId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class with ID " + studentRequestDto.getClassId() +
                        " not found in School with ID " + schoolId + " or the session is not active."));

        Session session = classEntity.getSession();

        // Check duplicate class for same name and session
        Optional<ClassEntity> duplicateClass = classEntityRepository.findByClassNameIgnoreCaseAndSessionIdAndSchoolId(classEntity.getClassName(), session.getId(), schoolId);

        if (duplicateClass.isPresent() && !duplicateClass.get().getId().equals(classEntity.getId()))
        {
            throw new AlreadyExistException("Class already exists with name: " + classEntity.getClassName() + " for the selected session.");
        }
        Student student = modelMapper.map(studentRequestDto, Student.class);
        student.setStatus(UserStatus.ACTIVE);
        student.setClassRollNumber(getNextRollNumber(studentRequestDto.getClassId(), schoolId));
        student.setCurrentClass(classEntity);
        student.setUser(user);
        student.setSchool(school);
        student.setSession(session);

        Student savedStudent = studentRepository.save(student);

        FeeStructure feeStructure = feeStructureRepository.findByClassEntity_IdAndSession_IdAndSchool_Id(studentRequestDto.getClassId(), session.getId(), schoolId).orElseThrow(() -> new ResourceNotFoundException("Fee structure not found for class and session"));

        List<Fee> feeEntries = new ArrayList<>();
        LocalDate currentMonth = session.getStartDate().withDayOfMonth(1);

        for (int i = 0; i < 12; i++)
        {
            Fee fee = new Fee();
            fee.setMonth(FeeMonth.valueOf(currentMonth.getMonth().toString()));
            fee.setYear(currentMonth.getYear());
            fee.setStatus(FeeStatus.PENDING);
            fee.setAmount(feeStructure.getFeesAmount());
            fee.setFeeStructure(feeStructure);
            fee.setDueDate(currentMonth.withDayOfMonth(15));
            fee.setStudent(student);
            fee.setSchool(school);

            feeEntries.add(fee);
            currentMonth = currentMonth.plusMonths(1);
        }

        feeRepository.saveAll(feeEntries);

        StudentEnrollments studentEnrollments = new StudentEnrollments();
        studentEnrollments.setStudent(savedStudent);
        studentEnrollments.setSchool(school);
        studentEnrollments.setClassEntity(classEntity);
        studentEnrollments.setSession(session);
        studentEnrollmentRepository.save(studentEnrollments);

        log.info("Student created successfully with PAN: {}", studentRequestDto.getPanNumber());

        StudentResponseDto responseDto = convertToDto(savedStudent);
        responseDto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
        responseDto.setFeeStatus(FeeStatus.PENDING);

        return responseDto;
    }

    @Override
    public boolean existsByPanNumber(String panNumber)
    {
        return studentRepository.existsById(panNumber);
    }

    @Transactional
    @Override
    public void markStudentsGraduateOrInActive(List<String> panNumbers, UserStatus status, Long schoolId)
    {
        List<Student> students = new ArrayList<>();
//        List<User> users = new ArrayList<>();

        for (String pan : panNumbers)
        {
            // FIXED: Use findByPanNumberIgnoreCaseAndSchool_Id instead of ...AndStatusActive
            // This allows changing status of INACTIVE/GRADUATED students back to ACTIVE
            Student student = studentRepository.findByPanNumberIgnoreCaseAndSchool_Id(pan, schoolId)
                    .orElseThrow(() -> new RuntimeException("Student not found with pan: " + pan));

//            User user = student.getUser();
//
//            if (user != null)
//            {
//                user.setEnabled(false);
//                users.add(user);
//            }

            student.setStatus(status);
            students.add(student);
        }

//        userRepository.saveAll(users);
        studentRepository.saveAll(students);
    }

    @Override
    public List<StudentResponseDto> getStudentsByClassId(Long classId, Long schoolId)
    {
        log.info("Fetching students by class ID: {}", classId);
        ClassEntity classEntity = classEntityRepository
                .findByIdAndSchoolIdAndSessionActive(classId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class with ID " + classId +
                        " not found in School with ID " + schoolId + " or the session is not active."));

        List<Student> activeStudents = studentRepository.findByCurrentClassIdAndSchoolIdAndStatusActive(classId, schoolId);

        return activeStudents.stream().map(student ->
        {
            StudentResponseDto dto = convertToDto(student);
            setFeeStatuses(dto, student.getPanNumber(), schoolId);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StudentResponseDto> getAllStudent(Long schoolId)
    {
        log.info("Fetching all students");
        return studentRepository.findAllBySchoolId(schoolId).stream().map(student ->
        {
            StudentResponseDto dto = convertToDto(student);
            setFeeStatuses(dto, student.getPanNumber(), schoolId);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StudentResponseDto> getActiveStudents(Long schoolId)
    {
        log.info("Fetching all active students");
        List<Student> activeStudents = studentRepository.findAllActiveStudentsBySchoolId(schoolId);

        return activeStudents.stream().map(student ->
        {
            StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
            if (student.getSession() != null)
            {
                dto.setSessionId(student.getSession().getId());
                dto.setSessionName(student.getSession().getName());
            }
            setFeeStatuses(dto, student.getPanNumber(), schoolId);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public StudentResponseDto getStudentByPAN(String pan, Long schoolId)
    {
        log.info("Fetching student with PAN: {}", pan);
        Student fetchedStudent = EntityFetcher.fetchStudentByPan(studentRepository, pan);

        StudentResponseDto dto = convertToDto(fetchedStudent);
        setFeeStatuses(dto, pan, schoolId);

        log.info("Student fetched successfully with PAN: {}", pan);
        return dto;
    }

    @Override
    public Student getActiveStudentByPan(String pan, Long schoolId)
    {
        return studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(pan, schoolId)
                .orElseThrow(() -> new RuntimeException("Active Student not found with pan: " + pan));
    }


    @Override
    public List<Student> getStudentsBySchoolIdAndPanNumbers(Long schoolId, List<String> panNumbers)
    {
        List<String> lowerCasePanNumbers = panNumbers.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return studentRepository.findBySchoolIdAndPanNumberInIgnoreCase(schoolId, lowerCasePanNumbers);
    }


    @Override
    public StudentResponseDto updateStudent(String pan, UpdateStudentInfo updateStudentInfo, Long schoolId)
    {
        log.info("Updating student with PAN: {}", pan);

        Student fetchedStudent = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(pan, schoolId)
                .orElseThrow(() -> new WrongArgumentException("Only active students can be changed. Student with PAN " + pan + " is currently INACTIVE or GRADUATE"));

        if (!fetchedStudent.getSession().isActive())
        {
            throw new WrongArgumentException("Cannot update student because the session is inactive");
        }

        // Store original class ID for comparison
        Long originalClassId = fetchedStudent.getCurrentClass() != null ? fetchedStudent.getCurrentClass().getId() : null;

        // Map basic fields
        modelMapper.map(updateStudentInfo, fetchedStudent);
        fetchedStudent.setPanNumber(pan);

        // Handle class update - prioritize classId over className
        if (updateStudentInfo.getClassId() != null)
        {
            ClassEntity classEntity = classEntityRepository.findByIdAndSchoolIdAndSessionActive(updateStudentInfo.getClassId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class with ID " + updateStudentInfo.getClassId() + 
                            " not found in School with ID " + schoolId + " or the session is not active."));
            fetchedStudent.setCurrentClass(classEntity);
            log.info("Updated student class to: {}", classEntity.getClassName());
        }
        else if (updateStudentInfo.getClassName() != null && !updateStudentInfo.getClassName().isEmpty())
        {
            // Parse className format "1-A" to get class and section
            String className = updateStudentInfo.getClassName();
            Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active session found for school ID: " + schoolId));
            
            // Append session name to className (e.g., "2-A" becomes "2-A - 2025-2026")
            String fullClassName = className.trim() + " - " + activeSession.getName().trim();
            log.info("Searching for class with full name: {}", fullClassName);
            
            ClassEntity classEntity = classEntityRepository.findByClassNameIgnoreCaseAndSessionIdAndSchoolId(fullClassName, activeSession.getId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class " + fullClassName + " not found in active session for school ID: " + schoolId));
            fetchedStudent.setCurrentClass(classEntity);
            log.info("Updated student class to: {} (ID: {})", classEntity.getClassName(), classEntity.getId());
        }

        Student updatedStudent = studentRepository.save(fetchedStudent);
        
        // Regenerate fees if class changed
        Long newClassId = updatedStudent.getCurrentClass() != null ? updatedStudent.getCurrentClass().getId() : null;
        boolean classChanged = (originalClassId != null && newClassId != null && !originalClassId.equals(newClassId));
        
        if (classChanged)
        {
            ClassEntity currentClass = updatedStudent.getCurrentClass();
            FeeStructure newFeeStructure = currentClass.getFeeStructures();
            Session activeSession = updatedStudent.getSession();
            
            log.info("Class changed from {} to {}. Updating fee structure for student {}...", 
                    originalClassId, newClassId, pan);
            
            if (newFeeStructure == null) {
                log.warn("No fee structure found for new class: {}. Cannot regenerate fees.", currentClass.getClassName());
            } else {
                try {
                    // Delete old unpaid/pending fees for this student
                    List<Fee> existingFees = feeRepository.findByStudentPanNumberAndSchoolIdOrderByYearAscMonthAsc(pan, schoolId);
                    
                    // Delete all existing fees to regenerate from scratch
                    if (!existingFees.isEmpty()) {
                        feeRepository.deleteAll(existingFees);
                        feeRepository.flush();
                        log.info("Deleted {} existing fees for student: {}", existingFees.size(), pan);
                    }
                    
                    // Generate new fees based on new class fee structure
                    List<Fee> newFeeEntries = new ArrayList<>();
                    LocalDate currentMonth = activeSession.getStartDate().withDayOfMonth(1);
                    
                    for (int i = 0; i < 12; i++)
                    {
                        Fee fee = new Fee();
                        fee.setMonth(FeeMonth.valueOf(currentMonth.getMonth().toString()));
                        fee.setYear(currentMonth.getYear());
                        fee.setStatus(FeeStatus.PENDING);
                        fee.setAmount(newFeeStructure.getFeesAmount());
                        fee.setFeeStructure(newFeeStructure);
                        fee.setDueDate(currentMonth.withDayOfMonth(15));
                        fee.setStudent(updatedStudent);
                        fee.setSchool(updatedStudent.getSchool());

                        newFeeEntries.add(fee);
                        currentMonth = currentMonth.plusMonths(1);
                    }
                    
                    feeRepository.saveAll(newFeeEntries);
                    feeRepository.flush();
                    log.info("Generated {} new fees for student: {} based on class: {} with fee amount: {}", 
                            newFeeEntries.size(), pan, currentClass.getClassName(), newFeeStructure.getFeesAmount());
                } catch (Exception e) {
                    log.error("Failed to regenerate fees for student: {}", pan, e);
                    // Don't fail the entire update if fee regeneration fails
                }
            }
        }

        log.info("Student updated successfully with PAN: {}", pan);

        StudentResponseDto dto = convertToDto(updatedStudent);
        setFeeStatuses(dto, pan, schoolId);

        return dto;
    }

    @Override
    public CurrentDayAttendance getStudentsPresentToday(Optional<Long> classId, Long schoolId)
    {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Student> presentStudents;
        CurrentDayAttendance result = new CurrentDayAttendance();

        if (classId.isPresent())
        {
            Long id = classId.get();
            ClassEntity classEntity = classEntityRepository.findByIdAndSchoolId(id, schoolId).orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + id));

            if (!classEntity.getSession().isActive())
            {
                throw new ResourceNotFoundException("Session is not active for class ID: " + id);
            }

            presentStudents = studentRepository.findStudentsPresentTodayByClassId(id, schoolId, startOfDay, endOfDay);

            result.setId(classEntity.getId());
            result.setClassId(classEntity.getId());
            result.setClassName(classEntity.getClassName());
        }
        else
        {
            presentStudents = studentRepository.findStudentsPresentToday(schoolId, startOfDay, endOfDay);
            result.setId(null);
            result.setClassId(null);
            result.setClassName("All Classes");
        }

        List<StudentAttendance> studentAttendances = presentStudents.stream().map(student ->
        {
            StudentAttendance dto = new StudentAttendance();
            dto.setPanNumber(student.getPanNumber());
            dto.setPresent(true);
            return dto;
        }).collect(Collectors.toList());

        result.setDate(LocalDate.now());
        result.setStudentAttendances(studentAttendances);

        return result;
    }

    @Override
    public Student findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(String panNumber, Long schoolId)
    {
        return studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(panNumber, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with PAN Number: " + panNumber +
                                " and ACTIVE status in school with ID: " + schoolId));
    }

    @Override
    public Student findByPanNumberIgnoreCaseAndSchool_IdAndStatusInactive(String panNumber, Long schoolId)
    {
        return studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusInactive(panNumber, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with PAN Number: " + panNumber +
                                " and ACTIVE status in school with ID: " + schoolId));
    }

    @Transactional
    @Override
    public void promoteStudentsToClass(List<String> panNumbers, Long classId, Long schoolId)
    {
        ClassEntity classEntity = classEntityRepository.findByIdAndSchoolIdAndSessionActive(classId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class with ID " + classId +
                        " not found in School with ID " + schoolId + " or the session is not active."));

        Session session = classEntity.getSession();
        School school = classEntity.getSchool();

        List<String> skippedPans = new ArrayList<>();
        List<Student> studentsToUpdate = panNumbers.stream()
                .map(pan ->
                {
                    Optional<Student> optionalStudent = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(pan, schoolId);
                    if (optionalStudent.isPresent())
                    {
                        return optionalStudent.get();
                    }
                    else
                    {
                        skippedPans.add(pan);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Skipped PANs (inactive or not found): {}", skippedPans);

        List<StudentEnrollments> enrollments = new ArrayList<>();

        for (Student student : studentsToUpdate)
        {
            // Update student data
            student.setCurrentClass(classEntity);
            student.setSession(session);
            student.setClassRollNumber(getNextRollNumber(classId, schoolId));

            // Create new enrollment
            StudentEnrollments studentEnrollment = new StudentEnrollments();
            studentEnrollment.setStudent(student);
            studentEnrollment.setSchool(school);
            studentEnrollment.setClassEntity(classEntity);
            studentEnrollment.setSession(session);

            enrollments.add(studentEnrollment);
        }

        // Save all enrollments and updated students
        studentEnrollmentRepository.saveAll(enrollments);
        studentRepository.saveAll(studentsToUpdate);
    }


    // Private helper methods

    private User fetchUserByPan(String panNumber)
    {
        return userRepository.findByPanNumberIgnoreCase(panNumber).orElseThrow(() -> new ResourceNotFoundException("Student User not found with PAN: " + panNumber));
    }

    private ClassEntity fetchClassEntity(Long classId)
    {
        return EntityFetcher.fetchByIdOrThrow(classEntityRepository, classId, EntityNames.CLASS_ENTITY);
    }

    private Session fetchSession(Long sessionId)
    {
        return EntityFetcher.fetchByIdOrThrow(sessionRepository, sessionId, EntityNames.SESSION);
    }

    private void validateClassAndSession(ClassEntity classEntity, Session session)
    {
        // Check duplicate class for same name and session
        Optional<ClassEntity> duplicateClass = classEntityRepository.findByClassNameIgnoreCaseAndSessionId(classEntity.getClassName(), session.getId());

        if (duplicateClass.isPresent() && !duplicateClass.get().getId().equals(classEntity.getId()))
        {
            throw new AlreadyExistException("Class already exists with name: " + classEntity.getClassName() + " for the selected session.");
        }

        if (!session.isActive())
        {
            log.warn("Cannot add student to inactive session with ID {}", session.getId());
            throw new WrongArgumentException("Cannot add student to an inactive session");
        }
    }




    private Integer getNextRollNumber(Long classId, Long schoolId)
    {
        Integer maxRoll = studentRepository.findMaxClassRollNumberByCurrentClassIdAndSchoolId(classId, schoolId);
        return (maxRoll != null ? maxRoll : 0) + 1;
    }

    private void validateStudentIsActive(Student student)
    {
        if (!student.getStatus().equals(UserStatus.ACTIVE))
        {
            log.info("Update not allowed. Student with PAN {} has status: {}", student.getPanNumber(), student.getStatus());
            throw new WrongArgumentException("Only active students can be changed. Student with PAN " + student.getPanNumber() + " is currently " + student.getStatus().name().toLowerCase() + ".");
        }
    }

    private void validateSessionIsActive(Student student)
    {
        if (!student.getSession().isActive())
        {
            throw new WrongArgumentException("Cannot update student because the session is inactive");
        }
    }

    private StudentResponseDto convertToDto(Student student)
    {
        StudentResponseDto dto = modelMapper.map(student, StudentResponseDto.class);
        dto.setClassId(student.getCurrentClass().getId());
        dto.setSessionId(student.getSession().getId());
        dto.setSessionName(student.getSession().getName());
        
        // Parse className (e.g., "1-A") into currentClass ("1") and section ("A")
        String className = student.getCurrentClass().getClassName();
        if (className != null && className.contains("-")) {
            String[] parts = className.split("-");
            dto.setCurrentClass(parts[0]); // e.g., "1", "10"
            dto.setSection(parts.length > 1 ? parts[1] : "A"); // e.g., "A", "B"
        } else {
            // If no section in className, use className as currentClass
            dto.setCurrentClass(className != null ? className : "N/A");
            dto.setSection("A"); // Default section
        }
        
        return dto;
    }

    private void setFeeStatuses(StudentResponseDto dto, String pan, Long schoolId)
    {
        FeeCatalogDto feeCatalog = feeService.getFeeCatalogByStudentPanNumber(pan, schoolId);

        LocalDate today = LocalDate.now();
        Month currentMonth = today.getMonth();
        int currentYear = today.getYear();

        Month previousMonth = currentMonth.minus(1);
        int previousYear = currentMonth == Month.JANUARY ? currentYear - 1 : currentYear;

        Optional<MonthlyFeeDto> currentMonthFeeOpt = feeCatalog.getMonthlyFees().stream().filter(fee -> fee.getYear() == currentYear && fee.getMonth().equalsIgnoreCase(currentMonth.name())).findFirst();

        Optional<MonthlyFeeDto> previousMonthFeeOpt = feeCatalog.getMonthlyFees().stream().filter(fee -> fee.getYear() == previousYear && fee.getMonth().equalsIgnoreCase(previousMonth.name())).findFirst();

        if (currentMonthFeeOpt.isPresent() && "paid".equalsIgnoreCase(currentMonthFeeOpt.get().getStatus()))
        {
            dto.setFeeStatus(FeeStatus.PAID);
            dto.setFeeCatalogStatus(FeeCatalogStatus.UP_TO_DATE);
            return;
        }

        // Check if any fee is overdue
        boolean anyFeeOverdue = feeCatalog.getMonthlyFees().stream()
                .anyMatch(fee -> "overdue".equalsIgnoreCase(fee.getStatus()));

        // If any fee is overdue, mark as OVERDUE
        if (anyFeeOverdue)
        {
            dto.setFeeStatus(FeeStatus.OVERDUE);
            dto.setFeeCatalogStatus(FeeCatalogStatus.OVERDUE);
            return;
        }

        // Otherwise, mark as PENDING
        dto.setFeeStatus(FeeStatus.PENDING);
        dto.setFeeCatalogStatus(FeeCatalogStatus.PENDING);
    }

    @Override
    public List<PreviousSchoolingRecordDto> getPreviousSchoolingRecords(String panNumber, Long schoolId)
    {
        log.info("Fetching previous schooling records for PAN: {}", panNumber);
        
        // Get all enrollments for this student (across all sessions)
        List<StudentEnrollments> enrollments = studentEnrollmentRepository
                .findByStudent_PanNumberIgnoreCaseAndStudent_School_IdOrderBySession_StartYearDesc(panNumber, schoolId);
        
        if (enrollments.isEmpty())
        {
            log.warn("No enrollment records found for PAN: {}", panNumber);
            return new ArrayList<>();
        }
        
        List<PreviousSchoolingRecordDto> records = new ArrayList<>();
        
        for (StudentEnrollments enrollment : enrollments)
        {
            // Skip current active session - we only want history
            if (enrollment.getSession().isActive())
            {
                continue;
            }
            
            PreviousSchoolingRecordDto record = buildSchoolingRecord(enrollment, schoolId);
            records.add(record);
        }
        
        log.info("Found {} previous schooling records for PAN: {}", records.size(), panNumber);
        return records;
    }
    
    private PreviousSchoolingRecordDto buildSchoolingRecord(StudentEnrollments enrollment, Long schoolId)
    {
        Student student = enrollment.getStudent();
        Session session = enrollment.getSession();
        ClassEntity classEntity = enrollment.getClassEntity();
        
        // Parse className for class and section
        String className = classEntity.getClassName();
        String classLabel = className;
        String section = "A";
        if (className != null && className.contains("-"))
        {
            String[] parts = className.split("-");
            classLabel = parts[0];
            section = parts.length > 1 ? parts[1] : "A";
        }
        
        // Calculate attendance
        List<Attendance> attendanceRecords = attendanceRepository
                .findByStudentPanNumberAndSessionId(student.getPanNumber(), session.getId());
        
        int totalPresent = 0;
        int totalAbsent = 0;
        for (Attendance attendance : attendanceRecords)
        {
            if (attendance.isPresent())
            {
                totalPresent++;
            }
            else
            {
                totalAbsent++;
            }
        }
        
        int totalDays = totalPresent + totalAbsent;
        double attendancePercentage = totalDays > 0 ? (totalPresent * 100.0) / totalDays : 0.0;
        
        // Get all scores for this student
        List<Score> allScores = scoreRepository.findByStudentPanNumber(student.getPanNumber());
        
        // Filter scores for this session (simple approach - get all scores and calculate)
        List<PreviousSchoolingRecordDto.ExamResultSummary> examResults = new ArrayList<>();
        double totalPercentage = 0.0;
        int examCount = 0;
        
        // Group scores by exam
        Map<Long, List<Score>> scoresByExam = allScores.stream()
                .filter(score -> score.getExam() != null)
                .collect(Collectors.groupingBy(score -> score.getExam().getId()));
        
        for (Map.Entry<Long, List<Score>> entry : scoresByExam.entrySet())
        {
            List<Score> examScores = entry.getValue();
            if (examScores.isEmpty()) continue;
            
            Exam exam = examScores.get(0).getExam();
            
            // Calculate total marks for this exam
            int obtainedMarks = 0;
            int totalMarks = 0;
            int subjectCount = 0;
            
            for (Score score : examScores)
            {
                if (score.getMarks() != null)
                {
                    obtainedMarks += score.getMarks().intValue();
                    subjectCount++;
                }
            }
            
            // Assume each subject is out of 100 marks (standard)
            totalMarks = subjectCount * 100;
            
            if (totalMarks > 0)
            {
                double percentage = (obtainedMarks * 100.0) / totalMarks;
                String grade = calculateGradeFromPercentage(percentage);
                
                PreviousSchoolingRecordDto.ExamResultSummary examSummary = PreviousSchoolingRecordDto.ExamResultSummary.builder()
                        .examName(exam.getName())
                        .examDate(null) // Exam doesn't have date in this model
                        .percentage(percentage)
                        .grade(grade)
                        .obtainedMarks(obtainedMarks)
                        .totalMarks(totalMarks)
                        .build();
                
                examResults.add(examSummary);
                totalPercentage += percentage;
                examCount++;
            }
        }
        
        // Calculate overall percentage (average of all exams)
        double overallPercentage = examCount > 0 ? totalPercentage / examCount : 0.0;
        String overallGrade = calculateGradeFromPercentage(overallPercentage);
        
        // Determine status
        String status = "COMPLETED";
        if (student.getStatus() == UserStatus.GRADUATED)
        {
            status = "COMPLETED";
        }
        else if (student.getStatus() == UserStatus.INACTIVE)
        {
            status = "TRANSFERRED";
        }
        
        return PreviousSchoolingRecordDto.builder()
                .sessionId(session.getId())
                .sessionName(session.getName())
                .className(classLabel)
                .section(section)
                .passingYear(session.getEndDate() != null ? session.getEndDate().getYear() : null)
                .status(status)
                .overallPercentage(overallPercentage)
                .overallGrade(overallGrade)
                .totalPresent(totalPresent)
                .totalAbsent(totalAbsent)
                .attendancePercentage(attendancePercentage)
                .examResults(examResults)
                .build();
    }
    
    private String calculateGradeFromPercentage(double percentage)
    {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        return "F";
    }
}
