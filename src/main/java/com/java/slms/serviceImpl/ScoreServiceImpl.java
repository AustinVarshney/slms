package com.java.slms.serviceImpl;

import com.java.slms.dto.ScoreRequestDTO;
import com.java.slms.dto.ScoreResponseDTO;
import com.java.slms.dto.StudentScore;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.ScoreService;
import com.java.slms.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
    public List<ScoreResponseDTO> createScoresOfStudents(ScoreRequestDTO scoreDto)
    {
        // Validate class
        ClassEntity classEntity = CommonUtil.fetchClassEntityByClassId(classEntityRepository, scoreDto.getClassId());

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
            Student student = CommonUtil.fetchStudentByPan(studentRepository, studentScore.getStudentPanNumber());

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
                throw new IllegalArgumentException("Marks '" + marks + "' exceed maximum allowed '" + exam.getMaximumMarks() + "' for exam '" + exam.getName() + "'");
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

        Student fetchedStudent = CommonUtil.fetchStudentByPan(studentRepository, panNumber);

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

        ClassEntity classEntity = CommonUtil.fetchClassEntityByClassId(classEntityRepository, classId);

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
        ClassEntity classEntity = CommonUtil.fetchClassEntityByClassId(classEntityRepository, classId);

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
        Student student = CommonUtil.fetchStudentByPan(studentRepository, panNumber);

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
        ClassEntity classEntity = CommonUtil.fetchClassEntityByClassId(classEntityRepository, classId);

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
        Student student = CommonUtil.fetchStudentByPan(studentRepository, panNumber);

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
}
