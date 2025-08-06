package com.java.slms.serviceImpl;

import com.java.slms.dto.ScoreRequestDTO;
import com.java.slms.dto.ScoreResponseDTO;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public ScoreResponseDTO createScore(ScoreRequestDTO scoreDto)
    {
        // Fetch and validate student
        Student student = studentRepository.findById(scoreDto.getStudentPanNumber()).orElseThrow(() ->
        {
            log.error("Student with PAN '{}' not found.", scoreDto.getStudentPanNumber());
            return new ResourceNotFoundException("Student with PAN '" + scoreDto.getStudentPanNumber() + "' not found.");
        });

        // Fetch and validate class by ID
        ClassEntity classEntity = classEntityRepository.findById(scoreDto.getClassId()).orElseThrow(() ->
        {
            log.error("Class with ID '{}' not found.", scoreDto.getClassId());
            return new ResourceNotFoundException("Class not found with ID: " + scoreDto.getClassId());
        });

        // Validate if student is enrolled in the class (using classId and studentPanNumber)
        boolean isEnrolled = studentRepository.existsByClassNameAndPanNumberIgnoreCase(classEntity.getClassName(), scoreDto.getStudentPanNumber());
        if (!isEnrolled)
        {
            log.error("Student with PAN '{}' is not enrolled in class '{}'.", scoreDto.getStudentPanNumber(), classEntity.getClassName());
            throw new ResourceNotFoundException("Student with PAN '" + scoreDto.getStudentPanNumber() + "' is not enrolled in class '" + classEntity.getClassName() + "'.");
        }

        // Fetch and validate subject by ID
        Subject subject = subjectRepository.findById(scoreDto.getSubjectId()).orElseThrow(() ->
        {
            log.error("Subject with ID '{}' not found.", scoreDto.getSubjectId());
            return new ResourceNotFoundException("Subject with ID '" + scoreDto.getSubjectId() + "' not found.");
        });

        // Validate if subject belongs to the correct class
        if (!subject.getClassEntity().getId().equals(scoreDto.getClassId()))
        {
            log.error("Subject with ID '{}' does not belong to class '{}'.", scoreDto.getSubjectId(), classEntity.getClassName());
            throw new ResourceNotFoundException("Subject with ID '" + scoreDto.getSubjectId() + "' does not belong to class '" + classEntity.getClassName() + "'.");
        }

        // Fetch and validate exam by ID
        Exam exam = examRepository.findById(scoreDto.getExamId()).orElseThrow(() ->
        {
            log.error("Exam with ID '{}' not found.", scoreDto.getExamId());
            return new ResourceNotFoundException("Exam with ID '" + scoreDto.getExamId() + "' not found.");
        });

        // Validate if exam belongs to the correct class
        if (!exam.getClassEntity().getId().equals(scoreDto.getClassId()))
        {
            log.error("Exam with ID '{}' does not belong to class '{}'.", scoreDto.getExamId(), classEntity.getClassName());
            throw new ResourceNotFoundException("Exam with ID '" + scoreDto.getExamId() + "' does not belong to class '" + classEntity.getClassName() + "'.");
        }

        // Check if the score already exists for this student, subject, and exam
        boolean isScoreExist = scoreRepository.existsByStudentPanNumberAndSubjectIdAndExamId(scoreDto.getStudentPanNumber(), scoreDto.getSubjectId(), scoreDto.getExamId());
        if (isScoreExist)
        {
            log.error("Score already exists for student '{}' in subject with ID '{}' for exam with ID '{}'.", scoreDto.getStudentPanNumber(), scoreDto.getSubjectId(), scoreDto.getExamId());
            throw new AlreadyExistException("Score already exists for student '" + scoreDto.getStudentPanNumber() + "' in subject with ID '" + scoreDto.getSubjectId() + "' for exam with ID '" + scoreDto.getExamId() + "'.");
        }

        Score score = new Score();
        score.setStudent(student);
        score.setSubject(subject);
        score.setExam(exam);
        score.setMarks(scoreDto.getMarks());
        score.setGrade(scoreDto.getGrade());
        Score savedScore = scoreRepository.save(score);

        // Prepare the response DTO and set the class name
        ScoreResponseDTO scoreResponseDTO = modelMapper.map(savedScore, ScoreResponseDTO.class);
        scoreResponseDTO.setClassName(classEntity.getClassName());
        scoreResponseDTO.setClassId(classEntity.getId());
        return scoreResponseDTO;
    }

    public List<ScoreResponseDTO> getScoresByStudentPan(String panNumber)
    {

        Student fetchedStudent = studentRepository.findById(panNumber).orElseThrow(() ->
        {
            log.error("Student with PAN number '{}' was not found.", panNumber);
            return new ResourceNotFoundException("Student with PAN number '" + panNumber + "' was not found.");
        });

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
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + examId));

        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + classId));

        // Fetch the scores based on exam and class
        List<Score> scores = scoreRepository.findByExam_IdAndStudent_CurrentClass_Id(examId, classId);

        // If no scores are found, throw an exception
        if (scores.isEmpty())
        {
            log.error("No scores found for exam ID: '{}' in class ID: '{}'", examId, classId);
            throw new ResourceNotFoundException("No scores found for exam ID '" + examId + "' in class ID '" + classId + "'.");
        }

        // Map Score entities to ScoreResponseDTO and set the className
        return scores.stream()
                .map(score ->
                {
                    ScoreResponseDTO dto = modelMapper.map(score, ScoreResponseDTO.class);
                    dto.setClassName(score.getStudent().getCurrentClass().getClassName());
                    dto.setClassId(score.getStudent().getCurrentClass().getId());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ScoreResponseDTO> getScoresByStudentPanAndExamName(String panNumber, String examName)
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
}
