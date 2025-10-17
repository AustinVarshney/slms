package com.java.slms.serviceImpl;

import com.java.slms.dto.*;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.StudentTermMarksService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentTermMarksServiceImpl implements StudentTermMarksService
{
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final SessionRepository sessionRepository;
    private final ClassExamRepository classExamRepository;
    private final SubjectRepository subjectRepository;
    private final StudentTermMarksRepository studentTermMarksRepository;

    @Override
    @Transactional
    public void addMarksOfStudentsByExamTypeAndClassIdInCurrentSession(CreateMarksDto marksDto, Long schoolId, Long subjectId, Long classId)
    {
        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found for school with ID: " + schoolId));

        ClassExam classExam = classExamRepository
                .findByClassEntityIdAndExamTypeIdAndSchoolIdWithActiveSession(classId, marksDto.getExamTypeId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassExam not found with given classId, examTypeId, and schoolId."));

        Subject subject = subjectRepository
                .findByIdAndSchoolId(subjectId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + subjectId + " for school ID: " + schoolId));

        List<StudentTermMarks> termMarksList = new ArrayList<>();

        for (MarksDto marks : marksDto.getMarks())
        {
            StudentEnrollments enrollment = studentEnrollmentRepository
                    .findByClassIdAndSchoolIdAndSessionIdAndPan(classId, schoolId, session.getId(), marks.getPanNumber())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Enrollment not found for PAN: " + marks.getPanNumber() +
                                    ", classId: " + classId +
                                    ", sessionId: " + session.getId() +
                                    ", schoolId: " + schoolId));

            boolean alreadyExists = studentTermMarksRepository.existsByEnrollmentAndClassExamAndSubject(enrollment, classExam, subject);
            if (alreadyExists)
            {
                throw new AlreadyExistException("Marks already exist for student with PAN: " + marks.getPanNumber() +
                        " in this exam and subject.");
            }

            StudentTermMarks termMarks = StudentTermMarks.builder()
                    .classExam(classExam)
                    .subject(subject)
                    .passingMarks(marksDto.getPassingMarks())
                    .maxMarks(marksDto.getMaxMarks())
                    .marksObtained(marks.getMarks())
                    .enrollment(enrollment)
                    .build();

            termMarksList.add(termMarks);
        }

        studentTermMarksRepository.saveAll(termMarksList);
    }

    @Override
    public StudentMarksResponseDto getStudentMarks(String panNumber)
    {
        List<StudentTermMarks> termMarks = studentTermMarksRepository
                .findByPanNumber(panNumber);

        if (termMarks.isEmpty())
        {
            throw new ResourceNotFoundException("No marks found for student with PAN: " + panNumber);
        }

        StudentEnrollments enrollment = termMarks.get(0).getEnrollment();
        Student student = enrollment.getStudent();
        ClassEntity classEntity = enrollment.getClassEntity();
        Session session = enrollment.getSession();

        Map<ClassExam, List<StudentTermMarks>> groupedByExam = termMarks.stream()
                .collect(Collectors.groupingBy(StudentTermMarks::getClassExam));

        List<ExamResultDto> examResults = new ArrayList<>();

        for (Map.Entry<ClassExam, List<StudentTermMarks>> entry : groupedByExam.entrySet())
        {
            ClassExam exam = entry.getKey();
            List<SubjectMarksDto> subjectDtos = entry.getValue().stream()
                    .map(mark -> new SubjectMarksDto(
                            mark.getSubject().getSubjectName(),
                            mark.getMarksObtained(),
                            mark.getMaxMarks(),
                            mark.getPassingMarks(),
                            mark.getMarksObtained() >= mark.getPassingMarks()
                    ))
                    .toList();

            examResults.add(new ExamResultDto(
                    exam.getExamType().getName(),
                    exam.getExamDate(),
                    subjectDtos
            ));
        }

        return new StudentMarksResponseDto(
                student.getName(),
                panNumber,
                classEntity.getClassName(),
                session.getName(),
                examResults
        );
    }

    @Override
    public List<StudentExamSummaryDto> getExamSummaryByPanNumber(String panNumber)
    {
        List<StudentExamSummaryDto> summaries = studentTermMarksRepository.findExamSummaryByPanNumber(panNumber);

        if (summaries.isEmpty())
        {
            throw new ResourceNotFoundException("No exam marks found for PAN: " + panNumber);
        }

        return summaries;
    }


}
