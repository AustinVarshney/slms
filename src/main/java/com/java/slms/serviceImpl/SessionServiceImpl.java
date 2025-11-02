package com.java.slms.serviceImpl;

import com.java.slms.dto.CreateOrUpdateSessionRequest;
import com.java.slms.dto.SessionDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService
{

    private final SessionRepository sessionRepository;
    private final SchoolRepository schoolRepository;
    private final ClassEntityRepository classEntityRepository;
    private final SubjectRepository subjectRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
    private final ExamTypeRepository examTypeRepository;
    private final ClassExamRepository classExamRepository;
    private final StaffLeaveAllowanceRepository staffLeaveAllowanceRepository;
    private final TimetableRepository timetableRepository;

    @Override
    @Transactional
    public SessionDto createSession(Long schoolId, CreateOrUpdateSessionRequest dto)
    {
        log.info("Creating session for school {} from {} to {}", schoolId, dto.getStartDate(), dto.getEndDate());

        // Only check for active session if the new session is being created as active
        if (dto.isActive() && sessionRepository.findBySchoolIdAndActiveTrue(schoolId).isPresent())
        {
            throw new WrongArgumentException("An active session already exists for this school. Deactivate it first or create this session as inactive.");
        }

        // Basic date validation (removed 12-month constraint and overlapping check)
        if (dto.getEndDate().isBefore(dto.getStartDate()) || dto.getEndDate().isEqual(dto.getStartDate()))
        {
            throw new WrongArgumentException("Session end date must be after start date.");
        }

        Session session = modelMapper.map(dto, Session.class);
        // Use the active status from the DTO instead of hardcoding to true
        session.setActive(dto.isActive());

        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));
        session.setSchool(school);

        Session saved = sessionRepository.save(session);
        log.info("Session created with ID {} for school {} with active status: {}", saved.getId(), schoolId, saved.isActive());

        // If this is an active session, delete all timetables from previous sessions
        if (saved.isActive()) {
            deleteOldTimetables(schoolId, saved.getId());
        }

        // Copy classes and subjects from previous session
        copyClassesAndSubjectsFromPreviousSession(schoolId, saved);

        return modelMapper.map(saved, SessionDto.class);
    }

    @Override
    public SessionDto updateSession(Long id, Long schoolId, CreateOrUpdateSessionRequest dto)
    {
        log.info("Updating session {} for school {}", id, schoolId);

        // Changed to findBySessionIdAndSchoolId to allow updating inactive sessions
        Session existing = sessionRepository.findBySessionIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id + " for schoolId: " + schoolId));

        // Basic date validation (removed 12-month constraint and overlapping check)
        if (dto.getEndDate().isBefore(dto.getStartDate()) || dto.getEndDate().isEqual(dto.getStartDate()))
        {
            throw new WrongArgumentException("Session end date must be after start date.");
        }

        modelMapper.map(dto, existing);
        Session saved = sessionRepository.save(existing);

        log.info("Session {} updated for school {}", saved.getId(), schoolId);
        return modelMapper.map(saved, SessionDto.class);
    }

    @Override
    @Transactional
    public void deleteSession(Long schoolId, Long id)
    {
        log.info("Deleting session {} for school {}", id, schoolId);

        Session session = sessionRepository.findBySessionIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id + " for schoolId: " + schoolId));
        
        if (session.isActive())
        {
            throw new WrongArgumentException("Cannot delete an active session. Deactivate it first.");
        }

        // Check if there are any students enrolled in this session
        List<Student> studentsInSession = studentRepository.findStudentsBySchoolIdAndSessionId(schoolId, id);
        if (!studentsInSession.isEmpty()) {
            throw new WrongArgumentException(
                "Cannot delete session with enrolled students. " +
                "There are " + studentsInSession.size() + " students currently enrolled in this session. " +
                "Please move or remove students first."
            );
        }

        // Delete all classes and their related entities for this session
        List<ClassEntity> classesInSession = classEntityRepository.findBySession_IdAndSchool_Id(id, schoolId);
        
        for (ClassEntity classEntity : classesInSession) {
            // Delete subjects first (they have FK to class)
            List<Subject> subjects = subjectRepository.findByClassEntityIdAndSchoolId(classEntity.getId(), schoolId);
            if (!subjects.isEmpty()) {
                subjectRepository.deleteAll(subjects);
                log.info("Deleted {} subjects for class {}", subjects.size(), classEntity.getClassName());
            }
            
            // Delete fee structure if exists
            feeStructureRepository.findByClassEntity_IdAndSession_IdAndSchool_Id(
                classEntity.getId(), id, schoolId
            ).ifPresent(feeStructure -> {
                feeStructureRepository.delete(feeStructure);
                log.info("Deleted fee structure for class {}", classEntity.getClassName());
            });
            
            // Now delete the class
            classEntityRepository.delete(classEntity);
            log.info("Deleted class {}", classEntity.getClassName());
        }

        // Finally delete the session
        sessionRepository.deleteById(session.getId());
        log.info("Session {} deleted successfully for school {}", id, schoolId);
    }

    @Override
    public List<SessionDto> getAllSessions(Long schoolId)
    {
        return sessionRepository.findAllBySchoolId(schoolId).stream().map(session -> modelMapper.map(session, SessionDto.class)).toList();
    }

    @Override
    public SessionDto getSessionById(Long schoolId, Long id)
    {
        // Changed to findBySessionIdAndSchoolId to allow viewing inactive sessions
        Session session = sessionRepository.findBySessionIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + id + " for schoolId: " + schoolId));

        return modelMapper.map(session, SessionDto.class);
    }

    @Override
    public SessionDto getCurrentSession(Long schoolId)
    {
        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId).orElseThrow(() -> new ResourceNotFoundException("No active session found for school with ID: " + schoolId));
        return modelMapper.map(session, SessionDto.class);
    }

    @Transactional
    @Override
    public void deactivateCurrentSession(Long schoolId)
    {
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId).orElseThrow(() -> new ResourceNotFoundException("No active session found to deactivate for school with ID: " + schoolId));

        activeSession.setActive(false);
        sessionRepository.save(activeSession);



        log.info("Deactivated session {} for school {}", activeSession.getId(), schoolId);
    }

    /**
     * Copy all classes, subjects, and fee structures from the most recent previous session to the new session
     */
    private void copyClassesAndSubjectsFromPreviousSession(Long schoolId, Session newSession) {
        log.info("Copying classes and subjects to new session {} for school {}", newSession.getId(), schoolId);

        // Find all sessions for this school ordered by start date (most recent first)
        List<Session> allSessions = sessionRepository.findAllBySchoolId(schoolId);
        
        // Find the most recent session before the new one
        Session previousSession = allSessions.stream()
                .filter(s -> !s.getId().equals(newSession.getId()))
                .filter(s -> s.getStartDate().isBefore(newSession.getStartDate()))
                .max((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                .orElse(null);

        if (previousSession == null) {
            log.info("No previous session found for school {}. No classes to copy.", schoolId);
            return;
        }

        log.info("Found previous session {} - copying its classes to new session {}", 
                previousSession.getName(), newSession.getName());

        // Get all classes from the previous session
        List<ClassEntity> previousClasses = classEntityRepository.findBySession_IdAndSchool_Id(
                previousSession.getId(), schoolId);

        if (previousClasses.isEmpty()) {
            log.info("No classes found in previous session {} for school {}", 
                    previousSession.getName(), schoolId);
            return;
        }

        // Copy each class to the new session
        for (ClassEntity oldClass : previousClasses) {
            // Create new class entity for the new session
            ClassEntity newClass = new ClassEntity();
            newClass.setClassName(oldClass.getClassName());
            newClass.setSession(newSession);
            newClass.setSchool(oldClass.getSchool());
            newClass.setClassTeacher(oldClass.getClassTeacher());

            // Save the new class
            ClassEntity savedClass = classEntityRepository.save(newClass);
            log.info("Copied class {} to new session", savedClass.getClassName());

            // Copy all subjects for this class
            List<Subject> oldSubjects = subjectRepository.findByClassEntityIdAndSchoolId(
                    oldClass.getId(), schoolId);

            for (Subject oldSubject : oldSubjects) {
                Subject newSubject = new Subject();
                newSubject.setSubjectName(oldSubject.getSubjectName());
                // CRITICAL: Set the NEW class entity from the NEW session, not the old one
                newSubject.setClassEntity(savedClass);
                newSubject.setTeacher(oldSubject.getTeacher());
                newSubject.setSchool(oldSubject.getSchool());

                Subject savedSubject = subjectRepository.save(newSubject);
                log.info("Copied subject {} (ID: {}) for class {} (ID: {}) in new session {}", 
                        savedSubject.getSubjectName(), savedSubject.getId(),
                        savedClass.getClassName(), savedClass.getId(), newSession.getName());
            }

            // Copy fee structure for this class
            feeStructureRepository.findByClassEntity_IdAndSession_IdAndSchool_Id(
                    oldClass.getId(), previousSession.getId(), schoolId
            ).ifPresent(oldFeeStructure -> {
                FeeStructure newFeeStructure = new FeeStructure();
                newFeeStructure.setFeesAmount(oldFeeStructure.getFeesAmount());
                newFeeStructure.setClassEntity(savedClass);
                newFeeStructure.setSession(newSession);
                newFeeStructure.setSchool(oldFeeStructure.getSchool());

                feeStructureRepository.save(newFeeStructure);
                log.info("Copied fee structure for class {} - Amount: {}", 
                        savedClass.getClassName(), newFeeStructure.getFeesAmount());
            });
        }

        log.info("Successfully copied {} classes from session {} to session {} for school {}", 
                previousClasses.size(), previousSession.getName(), newSession.getName(), schoolId);
        
        // Copy exam types and class exams from previous session
        copyExamTypesAndClassExams(previousSession, newSession, schoolId);
        
        // Copy staff leave allowances for active teachers
        copyStaffLeaveAllowances(previousSession, newSession, schoolId);
    }
    
    /**
     * Copy exam types and their class exam assignments from previous session to new session
     */
    private void copyExamTypesAndClassExams(Session previousSession, Session newSession, Long schoolId) {
        log.info("Copying exam types and class exams from session {} to {}", 
                previousSession.getName(), newSession.getName());
        
        try {
            // Get all exam types for this school (exam types are not session-specific in schema)
            List<com.java.slms.model.ExamType> examTypes = examTypeRepository.findAllBySchoolId(schoolId);
            
            if (examTypes.isEmpty()) {
                log.info("No exam types found for school {}", schoolId);
                return;
            }
            
            // Copy class exam assignments for each exam type
            for (com.java.slms.model.ExamType examType : examTypes) {
                // Get all class exams for this exam type in previous session
                List<com.java.slms.model.ClassExam> oldClassExams = classExamRepository
                        .findByExamType_IdAndClassEntity_Session_IdAndSchool_Id(
                                examType.getId(), previousSession.getId(), schoolId);
                
                for (com.java.slms.model.ClassExam oldClassExam : oldClassExams) {
                    // Find corresponding class in new session
                    String className = oldClassExam.getClassEntity().getClassName();
                    classEntityRepository.findByClassNameIgnoreCaseAndSessionIdAndSchoolId(
                            className, newSession.getId(), schoolId
                    ).ifPresent(newClass -> {
                        // Check if class exam already exists
                        boolean exists = classExamRepository.existsByClassAndExamTypeAndSchoolId(
                                newClass, examType, schoolId);
                        
                        if (!exists) {
                            com.java.slms.model.ClassExam newClassExam = com.java.slms.model.ClassExam.builder()
                                    .classEntity(newClass)
                                    .examType(examType)
                                    .school(oldClassExam.getSchool())
                                    .examDate(oldClassExam.getExamDate())
                                    .maxMarks(oldClassExam.getMaxMarks())
                                    .passingMarks(oldClassExam.getPassingMarks())
                                    .build();
                            
                            classExamRepository.save(newClassExam);
                            log.info("Copied class exam {} for class {} to new session", 
                                    examType.getName(), className);
                        }
                    });
                }
            }
            
            log.info("Successfully copied exam types and class exams to new session");
        } catch (Exception e) {
            log.error("Error copying exam types and class exams: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Copy staff leave allowances from previous session to new session
     */
    private void copyStaffLeaveAllowances(Session previousSession, Session newSession, Long schoolId) {
        log.info("Copying staff leave allowances from session {} to {}", 
                previousSession.getName(), newSession.getName());
        
        try {
            // Get all staff leave allowances from previous session
            List<com.java.slms.model.StaffLeaveAllowance> oldAllowances = 
                    staffLeaveAllowanceRepository.findBySessionAndSchoolId(previousSession, schoolId);
            
            for (com.java.slms.model.StaffLeaveAllowance oldAllowance : oldAllowances) {
                // Check if allowance already exists for this staff in new session
                boolean exists = staffLeaveAllowanceRepository.existsByStaffAndSessionAndSchoolId(
                        oldAllowance.getStaff(), newSession, schoolId);
                
                if (!exists) {
                    com.java.slms.model.StaffLeaveAllowance newAllowance = new com.java.slms.model.StaffLeaveAllowance();
                    newAllowance.setStaff(oldAllowance.getStaff());
                    newAllowance.setSession(newSession);
                    newAllowance.setSchool(oldAllowance.getSchool());
                    newAllowance.setAllowedLeaves(oldAllowance.getAllowedLeaves());
                    
                    staffLeaveAllowanceRepository.save(newAllowance);
                    log.info("Copied leave allowance for staff {} to new session (allowed: {})", 
                            oldAllowance.getStaff().getEmail(), oldAllowance.getAllowedLeaves());
                }
            }
            
            log.info("Successfully copied staff leave allowances to new session");
        } catch (Exception e) {
            log.error("Error copying staff leave allowances: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Delete all timetables from old sessions when a new active session is created
     * This ensures only the current session's timetable is retained
     */
    private void deleteOldTimetables(Long schoolId, Long newSessionId) {
        log.info("Deleting old timetables for school {} (keeping only session {})", schoolId, newSessionId);
        
        try {
            // Get all sessions for this school except the new one
            List<Session> allSessions = sessionRepository.findAllBySchoolId(schoolId);
            
            int totalDeleted = 0;
            for (Session oldSession : allSessions) {
                // Skip the new session
                if (oldSession.getId().equals(newSessionId)) {
                    continue;
                }
                
                // Find and delete all timetables for this old session
                List<TimeTable> oldTimetables = timetableRepository.findBySchoolIdAndSessionId(
                        schoolId, oldSession.getId());
                
                if (!oldTimetables.isEmpty()) {
                    timetableRepository.deleteAll(oldTimetables);
                    totalDeleted += oldTimetables.size();
                    log.info("Deleted {} timetable entries from session {} (ID: {})", 
                            oldTimetables.size(), oldSession.getName(), oldSession.getId());
                }
            }
            
            log.info("Successfully deleted {} old timetable entries for school {}. Only new session {} timetables remain.", 
                    totalDeleted, schoolId, newSessionId);
        } catch (Exception e) {
            log.error("Error deleting old timetables: {}", e.getMessage(), e);
            // Don't throw exception - timetable cleanup failure shouldn't block session creation
        }
    }
}
