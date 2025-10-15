package com.java.slms.serviceImpl;

import com.java.slms.dto.*;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.ScoreService;
import com.java.slms.util.EntityFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreServiceImpl implements ScoreService
{

    private final ScoreRepository scoreRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final ClassEntityRepository classEntityRepository;
    private final ExamRepository examRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public List<ScoreResponseDTO> createScoresOfStudents(ScoreRequestDTO scoreDto)
    {
        // Validate class
        ClassEntity classEntity = EntityFetcher.fetchClassEntityByClassId(classEntityRepository, scoreDto.getClassId());

        // Validate subject
        Subject subject = subjectRepository.findById(scoreDto.getSubjectId()).orElseThrow(() ->
        {
            log.error("Subject with ID '{}' not found.", scoreDto.getSubjectId());
            return new ResourceNotFoundException("Subject with ID '" + scoreDto.getSubjectId() + "' not found.");
        });
        if (!subject.getClassEntity().getId().equals(scoreDto.getClassId()))
        {
            log.error("Subject with ID '{}' does not belong to class '{}'.", scoreDto.getSubjectId(), classEntity.getClassName());
            throw new ResourceNotFoundException("Subject with ID '" + scoreDto.getSubjectId() + "' does not belong to class '" + classEntity.getClassName() + "'.");
        }

        // Validate exam
        Exam exam = examRepository.findById(scoreDto.getExamId()).orElseThrow(() ->
        {
            log.error("Exam with ID '{}' not found.", scoreDto.getExamId());
            return new ResourceNotFoundException("Exam with ID '" + scoreDto.getExamId() + "' not found.");
        });
        if (!exam.getClassEntity().getId().equals(scoreDto.getClassId()))
        {
            log.error("Exam with ID '{}' does not belong to class '{}'.", scoreDto.getExamId(), classEntity.getClassName());
            throw new ResourceNotFoundException("Exam with ID '" + scoreDto.getExamId() + "' does not belong to class '" + classEntity.getClassName() + "'.");
        }

        List<ScoreResponseDTO> responses = new ArrayList<>();

        for (StudentScore studentScore : scoreDto.getStudentPanNumbers())
        {
            String panNumber = studentScore.getStudentPanNumber();

            // Validate student
            Student student = EntityFetcher.fetchStudentByPan(studentRepository, studentScore.getStudentPanNumber());

            // Check enrollment
            boolean isEnrolled = studentRepository.existsByClassNameAndPanNumberIgnoreCase(classEntity.getClassName(), panNumber);
            if (!isEnrolled)
            {
                log.error("Student with PAN '{}' is not enrolled in class '{}'.", panNumber, classEntity.getClassName());
                throw new ResourceNotFoundException("Student with PAN '" + panNumber + "' is not enrolled in class '" + classEntity.getClassName() + "'.");
            }

            // Check for existing score
            boolean isScoreExist = scoreRepository.existsByStudentPanNumberAndSubjectIdAndExamId(panNumber, scoreDto.getSubjectId(), scoreDto.getExamId());
            if (isScoreExist)
            {
                log.error("Score already exists for student '{}' in subject with ID '{}' for exam with ID '{}'.", panNumber, scoreDto.getSubjectId(), scoreDto.getExamId());
                throw new AlreadyExistException("Score already exists for student '" + panNumber + "' in subject with ID '" + scoreDto.getSubjectId() + "' for exam with ID '" + scoreDto.getExamId() + "'.");
            }

            // Compose per-student marks/grade or fallback to global if null
            Double marks = studentScore.getMarks() != null ? studentScore.getMarks() : scoreDto.getMarks();
            String grade = studentScore.getGrade() != null ? studentScore.getGrade() : scoreDto.getGrade();

            if (marks != null && marks > exam.getMaximumMarks())
            {
                log.error("Marks '{}' exceed maximum allowed '{}' for exam '{}'", marks, exam.getMaximumMarks(), exam.getName());
                throw new ResourceNotFoundException("Marks '" + marks + "' exceed maximum allowed '" + exam.getMaximumMarks() + "' for exam '" + exam.getName() + "'");
            }

            // Save score
            Score score = new Score();
            score.setStudent(student);
            score.setSubject(subject);
            score.setExam(exam);
            score.setMarks(marks);
            score.setGrade(grade);

            Score savedScore = scoreRepository.save(score);

            ScoreResponseDTO resp = modelMapper.map(savedScore, ScoreResponseDTO.class);
            resp.setClassName(classEntity.getClassName());
            resp.setClassId(classEntity.getId());
            // Optionally: Set subject/exam names using subject.getName(), exam.getName()
            resp.setExamName(exam.getName());
            resp.setStudentPanNumber(panNumber);

            responses.add(resp);
        }
        return responses;
    }

    public List<ScoreResponseDTO> getScoresByStudentPan(String panNumber)
    {

        Student fetchedStudent = EntityFetcher.fetchStudentByPan(studentRepository, panNumber);

        List<Score> scores = scoreRepository.findByStudentPanNumber(panNumber);

        if (scores.isEmpty())
        {
            log.error("No scores found for student with PAN: {}", panNumber);
            throw new ResourceNotFoundException("No scores found for student with PAN: " + panNumber);
        }

        return scores.stream().map(score ->
        {
            ScoreResponseDTO dto = modelMapper.map(score, ScoreResponseDTO.class);
            dto.setClassName(fetchedStudent.getCurrentClass().getClassName());
            dto.setClassId(fetchedStudent.getCurrentClass().getId());
            return dto;
        }).toList();
    }

    @Override
    public List<ScoreResponseDTO> getScoresByExamIdAndClassId(Long examId, Long classId)
    {
        // Fetch the exam and class to ensure they exist
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));

        ClassEntity classEntity = EntityFetcher.fetchClassEntityByClassId(classEntityRepository, classId);

        // Fetch the scores based on exam and class
        List<Score> scores = scoreRepository.findByExam_IdAndStudent_CurrentClass_Id(examId, classId);

        // If no scores are found, throw an exception
        if (scores.isEmpty())
        {
            log.error("No scores found for exam ID: '{}' in class ID: '{}'", examId, classId);
            throw new ResourceNotFoundException("No scores found for exam ID '" + examId + "' in class ID '" + classId + "'.");
        }

        // Map Score entities to ScoreResponseDTO and set the className
        return scores.stream().map(score ->
        {
            ScoreResponseDTO dto = modelMapper.map(score, ScoreResponseDTO.class);
            dto.setClassName(score.getStudent().getCurrentClass().getClassName());
            dto.setClassId(score.getStudent().getCurrentClass().getId());
            return dto;
        }).toList();
    }

    @Override
    public ScoreResponseDTO updateScoreByExamIdClassIdSubjectIdAndPanNumber(Long examId, Long classId, Long subjectId, String panNumber, StudentScore studentScore)
    {

        // Validate ClassEntity existence
        ClassEntity classEntity = EntityFetcher.fetchClassEntityByClassId(classEntityRepository, classId);

        // Validate Subject existence and class relation
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() ->
        {
            log.error("Subject with ID '{}' not found.", subjectId);
            return new ResourceNotFoundException("Subject with ID '" + subjectId + "' not found.");
        });
        if (!subject.getClassEntity().getId().equals(classId))
        {
            log.error("Subject with ID '{}' does not belong to class '{}'.", subjectId, classEntity.getClassName());
            throw new ResourceNotFoundException("Subject with ID '" + subjectId + "' does not belong to class '" + classEntity.getClassName() + "'.");
        }

        // Validate Exam existence and class relation
        Exam exam = examRepository.findById(examId).orElseThrow(() ->
        {
            log.error("Exam with ID '{}' not found.", examId);
            return new ResourceNotFoundException("Exam with ID '" + examId + "' not found.");
        });
        if (!exam.getClassEntity().getId().equals(classId))
        {
            log.error("Exam with ID '{}' does not belong to class '{}'.", examId, classEntity.getClassName());
            throw new ResourceNotFoundException("Exam with ID '" + examId + "' does not belong to class '" + classEntity.getClassName() + "'.");
        }

        // Validate Student existence
        Student student = EntityFetcher.fetchStudentByPan(studentRepository, panNumber);

        // Verify student's enrollment in class
        boolean isEnrolled = studentRepository.existsByClassNameAndPanNumberIgnoreCase(classEntity.getClassName(), panNumber);
        if (!isEnrolled)
        {
            log.error("Student with PAN '{}' is not enrolled in class '{}'.", panNumber, classEntity.getClassName());
            throw new ResourceNotFoundException("Student with PAN '" + panNumber + "' is not enrolled in class '" + classEntity.getClassName() + "'.");
        }

        // Fetch the existing Score by composite keys: studentPanNumber, subjectId, examId and classId filtered via Subject.classEntity
        Optional<Score> scoreOpt = scoreRepository.findByStudentPanNumberAndClassIdAndSubjectIdAndExamId(panNumber, classId, subjectId, examId);

        Score score = scoreOpt.orElseThrow(() ->
        {
            String errMsg = String.format("Score not found for student '%s', class ID '%d', subject ID '%d', exam ID '%d'.", panNumber, classId, subjectId, examId);
            log.error(errMsg);
            return new ResourceNotFoundException(errMsg);
        });

        // Update the marks and grade using StudentScore data (null checks to avoid overwriting with null)
        if (studentScore.getMarks() != null)
        {
            score.setMarks(studentScore.getMarks());
        }
        if (studentScore.getGrade() != null && !studentScore.getGrade().isEmpty())
        {
            score.setGrade(studentScore.getGrade());
        }

        Score updatedScore = scoreRepository.save(score);

        ScoreResponseDTO responseDTO = modelMapper.map(updatedScore, ScoreResponseDTO.class);

        responseDTO.setClassId(classEntity.getId());
        responseDTO.setClassName(classEntity.getClassName());
        responseDTO.setSubjectId(subject.getId());
        responseDTO.setSubjectName(subject.getSubjectName());
        responseDTO.setExamId(exam.getId());
        responseDTO.setExamName(exam.getName());
        responseDTO.setStudentPanNumber(panNumber);

        return responseDTO;
    }

    @Override
    public void deleteScoreByExamIdClassIdSubjectIdAndPanNumber(
            Long examId,
            Long classId,
            Long subjectId,
            String panNumber)
    {

        // Validate ClassEntity existence
        ClassEntity classEntity = EntityFetcher.fetchClassEntityByClassId(classEntityRepository, classId);

        // Validate Subject existence and class relation
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                {
                    log.error("Subject with ID '{}' not found.", subjectId);
                    return new ResourceNotFoundException("Subject with ID '" + subjectId + "' not found.");
                });
        if (!subject.getClassEntity().getId().equals(classId))
        {
            String message = String.format("Subject with ID '%d' does not belong to class '%s'.", subjectId, classEntity.getClassName());
            log.error(message);
            throw new ResourceNotFoundException(message);
        }

        // Validate Exam existence and class relation
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                {
                    log.error("Exam with ID '{}' not found.", examId);
                    return new ResourceNotFoundException("Exam with ID '" + examId + "' not found.");
                });
        if (!exam.getClassEntity().getId().equals(classId))
        {
            String message = String.format("Exam with ID '%d' does not belong to class '%s'.", examId, classEntity.getClassName());
            log.error(message);
            throw new ResourceNotFoundException(message);
        }

        // Validate Student existence
        Student student = EntityFetcher.fetchStudentByPan(studentRepository, panNumber);

        // Verify student's enrollment in class
        boolean isEnrolled = studentRepository.existsByClassNameAndPanNumberIgnoreCase(classEntity.getClassName(), panNumber);
        if (!isEnrolled)
        {
            String message = String.format("Student with PAN '%s' is not enrolled in class '%s'.", panNumber, classEntity.getClassName());
            log.error(message);
            throw new ResourceNotFoundException(message);
        }

        // Locate the existing Score entity
        Optional<Score> scoreOpt = scoreRepository.findByStudentPanNumberAndClassIdAndSubjectIdAndExamId(
                panNumber, classId, subjectId, examId);

        Score score = scoreOpt.orElseThrow(() ->
        {
            String errMsg = String.format("Score not found for student '%s', class ID '%d', subject ID '%d', exam ID '%d'.",
                    panNumber, classId, subjectId, examId);
            log.error(errMsg);
            return new ResourceNotFoundException(errMsg);
        });

        // Delete the Score
        scoreRepository.delete(score);

        log.info("Deleted Score with ID '{}' for student '{}', subject '{}', exam '{}', class '{}'.",
                score.getId(), panNumber, subjectId, examId, classId);
    }


    @Override
    public List<ScoreResponseDTO> getScoresByStudentPanAndExamName(String panNumber, Long examId)
    {
        return List.of();
    }

    @Override
    public List<ScoreResponseDTO> getScoresByStudentPanAndExamId(String panNumber, Long examId)
    {
        return List.of();
    }

    @Override
    public List<ScoreResponseDTO> getScoresByStudentPanAndSubjectName(String panNumber, String subjectName)
    {
        return List.of();
    }

    @Override
    public List<ScoreResponseDTO> getScoresByExamNameAndSubjectNameAndClassName(String examName, String subjectName, String className)
    {
        return List.of();
    }
    
    @Override
    @Transactional
    public List<ScoreResponseDTO> bulkUpdateScores(BulkScoreUpdateDTO bulkScoreUpdateDTO) {
        List<ScoreResponseDTO> responses = new ArrayList<>();
        
        // Validate class and subject
        ClassEntity classEntity = classEntityRepository.findById(bulkScoreUpdateDTO.getClassId())
            .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        
        Subject subject = subjectRepository.findById(bulkScoreUpdateDTO.getSubjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        
        // Exam is optional
        Exam exam = null;
        if (bulkScoreUpdateDTO.getExamId() != null) {
            exam = examRepository.findById(bulkScoreUpdateDTO.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        }
        
        // Process each student score
        for (BulkScoreUpdateDTO.StudentScoreEntry entry : bulkScoreUpdateDTO.getScores()) {
            Student student = studentRepository.findById(entry.getStudentPanNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + entry.getStudentPanNumber()));
            
            // Check if score already exists
            Optional<Score> existingScore;
            if (exam != null) {
                existingScore = scoreRepository.findByStudentAndExamAndSubject(student, exam, subject);
            } else {
                // If no exam, find by student and subject only (for the current class)
                existingScore = scoreRepository.findByStudentAndSubjectAndExamIsNull(student, subject);
            }
            
            Score score;
            if (existingScore.isPresent()) {
                // Update existing score
                score = existingScore.get();
                score.setMarks(entry.getMarks());
                score.setGrade(entry.getGrade());
            } else {
                // Create new score
                score = new Score();
                score.setStudent(student);
                score.setExam(exam); // Can be null
                score.setSubject(subject);
                score.setMarks(entry.getMarks());
                score.setGrade(entry.getGrade());
            }
            
            score = scoreRepository.save(score);
            
            // Convert to response DTO
            ScoreResponseDTO responseDTO = new ScoreResponseDTO();
            responseDTO.setId(score.getId());
            responseDTO.setStudentPanNumber(student.getPanNumber());
            responseDTO.setSubjectId(subject.getId());
            responseDTO.setSubjectName(subject.getSubjectName());
            if (exam != null) {
                responseDTO.setExamId(exam.getId());
                responseDTO.setExamName(exam.getName());
            }
            responseDTO.setClassId(classEntity.getId());
            responseDTO.setClassName(classEntity.getClassName());
            responseDTO.setMarks(score.getMarks());
            responseDTO.setGrade(score.getGrade());
            
            responses.add(responseDTO);
        }
        
        return responses;
    }
    
    @Override
    public ClassResultsDTO getClassResultsForExam(Long classId, Long examId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
            .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        
        ClassResultsDTO resultsDTO = new ClassResultsDTO();
        resultsDTO.setClassId(classEntity.getId());
        resultsDTO.setClassName(classEntity.getClassName());
        resultsDTO.setSection(classEntity.getClassName()); // Using className as section is not available
        resultsDTO.setExamId(exam.getId());
        resultsDTO.setExamName(exam.getName());
        
        // Get all subjects for this class
        List<Subject> subjects = subjectRepository.findByClassEntity_Id(classId);
        List<ClassResultsDTO.SubjectInfo> subjectInfos = new ArrayList<>();
        for (Subject subject : subjects) {
            ClassResultsDTO.SubjectInfo subjectInfo = new ClassResultsDTO.SubjectInfo();
            subjectInfo.setSubjectId(subject.getId());
            subjectInfo.setSubjectName(subject.getSubjectName());
            subjectInfo.setMaxMarks(exam.getMaximumMarks());
            subjectInfos.add(subjectInfo);
        }
        resultsDTO.setSubjects(subjectInfos);
        
        // Get all students in this class
        List<Student> students = studentRepository.findByCurrentClass_Id(classId);
        List<ClassResultsDTO.StudentResult> studentResults = new ArrayList<>();
        
        for (Student student : students) {
            ClassResultsDTO.StudentResult studentResult = new ClassResultsDTO.StudentResult();
            studentResult.setPanNumber(student.getPanNumber());
            studentResult.setStudentName(student.getName());
            studentResult.setRollNumber(String.valueOf(student.getClassRollNumber()));
            
            List<ClassResultsDTO.SubjectMarks> subjectMarksList = new ArrayList<>();
            double totalObtained = 0;
            double totalMax = 0;
            
            for (Subject subject : subjects) {
                Optional<Score> scoreOpt = scoreRepository.findByStudentAndExamAndSubject(student, exam, subject);
                
                ClassResultsDTO.SubjectMarks subjectMarks = new ClassResultsDTO.SubjectMarks();
                subjectMarks.setSubjectId(subject.getId());
                subjectMarks.setSubjectName(subject.getSubjectName());
                
                if (scoreOpt.isPresent()) {
                    Score score = scoreOpt.get();
                    subjectMarks.setMarks(score.getMarks());
                    subjectMarks.setGrade(score.getGrade());
                    totalObtained += score.getMarks();
                } else {
                    subjectMarks.setMarks(0.0);
                    subjectMarks.setGrade("-");
                }
                
                totalMax += exam.getMaximumMarks();
                subjectMarksList.add(subjectMarks);
            }
            
            studentResult.setMarks(subjectMarksList);
            studentResult.setTotalObtained(totalObtained);
            studentResult.setTotalMax(totalMax);
            studentResult.setPercentage(totalMax > 0 ? (totalObtained / totalMax) * 100 : 0);
            studentResult.setOverallGrade(calculateGrade(studentResult.getPercentage()));
            
            studentResults.add(studentResult);
        }
        
        resultsDTO.setStudentResults(studentResults);
        return resultsDTO;
    }
    
    @Override
    public StudentResultsDTO getStudentAllResults(String panNumber) {
        Student student = studentRepository.findById(panNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        StudentResultsDTO resultsDTO = new StudentResultsDTO();
        resultsDTO.setStudentPanNumber(student.getPanNumber());
        resultsDTO.setStudentName(student.getName());
        resultsDTO.setClassName(student.getCurrentClass().getClassName());
        resultsDTO.setSection(student.getCurrentClass().getClassName()); // Using className as section is not available
        
        // Get all exams for this student's class
        List<Exam> exams = examRepository.findByClassEntity_Id(student.getCurrentClass().getId());
        List<StudentResultsDTO.ExamResult> examResults = new ArrayList<>();
        
        for (Exam exam : exams) {
            StudentResultsDTO.ExamResult examResult = new StudentResultsDTO.ExamResult();
            examResult.setExamId(exam.getId());
            examResult.setExamName(exam.getName());
            examResult.setExamDate(exam.getExamDate() != null ? exam.getExamDate().toString() : "");
            
            // Get scores for all subjects in this exam
            List<Score> scores = scoreRepository.findByStudentAndExam(student, exam);
            List<StudentResultsDTO.SubjectScore> subjectScores = new ArrayList<>();
            
            double totalObtained = 0;
            double totalMax = 0;
            
            for (Score score : scores) {
                StudentResultsDTO.SubjectScore subjectScore = new StudentResultsDTO.SubjectScore();
                subjectScore.setSubjectId(score.getSubject().getId());
                subjectScore.setSubjectName(score.getSubject().getSubjectName());
                subjectScore.setMarks(score.getMarks());
                subjectScore.setMaxMarks(exam.getMaximumMarks());
                subjectScore.setGrade(score.getGrade());
                
                subjectScores.add(subjectScore);
                totalObtained += score.getMarks();
                totalMax += exam.getMaximumMarks();
            }
            
            examResult.setSubjectScores(subjectScores);
            examResult.setTotalMarks(totalMax);
            examResult.setObtainedMarks(totalObtained);
            examResult.setPercentage(totalMax > 0 ? (totalObtained / totalMax) * 100 : 0);
            examResult.setOverallGrade(calculateGrade(examResult.getPercentage()));
            
            examResults.add(examResult);
        }
        
        resultsDTO.setExamResults(examResults);
        return resultsDTO;
    }
    
    @Override
    public List<ScoreResponseDTO> getScoresByClassIdSubjectIdAndExamId(Long classId, Long subjectId, Long examId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
            .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        
        List<Score> scores = scoreRepository.findByExamAndSubject(exam, subject);
        List<ScoreResponseDTO> responseDTOs = new ArrayList<>();
        
        for (Score score : scores) {
            // Filter by class
            if (!score.getStudent().getCurrentClass().getId().equals(classId)) {
                continue;
            }
            
            ScoreResponseDTO responseDTO = new ScoreResponseDTO();
            responseDTO.setId(score.getId());
            responseDTO.setStudentPanNumber(score.getStudent().getPanNumber());
            responseDTO.setSubjectId(subject.getId());
            responseDTO.setSubjectName(subject.getSubjectName());
            responseDTO.setExamId(exam.getId());
            responseDTO.setExamName(exam.getName());
            responseDTO.setClassId(classEntity.getId());
            responseDTO.setClassName(classEntity.getClassName());
            responseDTO.setMarks(score.getMarks());
            responseDTO.setGrade(score.getGrade());
            
            responseDTOs.add(responseDTO);
        }
        
        return responseDTOs;
    }
    
    private String calculateGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        return "F";
    }
}
