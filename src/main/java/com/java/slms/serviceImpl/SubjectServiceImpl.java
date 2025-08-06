package com.java.slms.serviceImpl;

import com.java.slms.dto.SubjectDto;
import com.java.slms.dto.SubjectsBulkDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Subject;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.SubjectRepository;
import com.java.slms.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService
{

    private final SubjectRepository subjectRepository;
    private final ClassEntityRepository classEntityRepository;
    private final ModelMapper modelMapper;

    public SubjectDto addSubject(SubjectDto subjectDto)
    {
        ClassEntity classEntity = classEntityRepository.findById(subjectDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + subjectDto.getClassId()));

        if (subjectRepository.existsBySubjectNameIgnoreCaseAndClassEntity_Id(subjectDto.getSubjectName(), subjectDto.getClassId()))
        {
            throw new AlreadyExistException("Subject '" + subjectDto.getSubjectName() + "' already exists in class with ID '" + subjectDto.getClassId() + "'.");
        }

        Subject subject = new Subject();
        subject.setSubjectName(subjectDto.getSubjectName());
        subject.setClassEntity(classEntity);

        Subject saved = subjectRepository.save(subject);
        SubjectDto savedDto = modelMapper.map(saved, SubjectDto.class);
        savedDto.setClassId(classEntity.getId());

        return savedDto;
    }

    @Override
    public List<SubjectDto> getAllSubjects()
    {
        List<Subject> subjects = subjectRepository.findAll();
        List<SubjectDto> dtos = new ArrayList<>();
        for (Subject s : subjects)
        {
            SubjectDto dto = modelMapper.map(s, SubjectDto.class);
            dto.setClassName(s.getClassEntity().getClassName());
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public SubjectDto getSubjectById(Long id)
    {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID '" + id + "' not found."));

        SubjectDto dto = modelMapper.map(subject, SubjectDto.class);

        dto.setClassId(subject.getClassEntity().getId());

        return dto;
    }

    @Override
    public void deleteSubject(Long subjectId, Long classId)
    {
        if (!classEntityRepository.existsById(classId))
        {
            throw new ResourceNotFoundException("Class with ID '" + classId + "' not found.");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID '" + subjectId + "' not found."));

        // Check if the subject belongs to the given class
        if (!subject.getClassEntity().getId().equals(classId))
        {
            throw new ResourceNotFoundException("Subject with ID '" + subjectId + "' does not belong to class with ID '" + classId + "'.");
        }

        subjectRepository.delete(subject);
    }

    @Override
    public SubjectDto updateSubjectById(Long subjectId, SubjectDto subjectDto)
    {
        Subject existing = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + subjectId));

        ClassEntity classEntity = classEntityRepository.findById(subjectDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + subjectDto.getClassId()));

        boolean subjectExists = subjectRepository.existsBySubjectNameIgnoreCaseAndClassEntity_Id(subjectDto.getSubjectName(), subjectDto.getClassId());
        if (!existing.getSubjectName().equalsIgnoreCase(subjectDto.getSubjectName()) && subjectExists)
        {
            throw new AlreadyExistException("Subject '" + subjectDto.getSubjectName() + "' already exists in class with ID: " + subjectDto.getClassId());
        }

        existing.setSubjectName(subjectDto.getSubjectName());
        existing.setClassEntity(classEntity);

        Subject updated = subjectRepository.save(existing);

        SubjectDto updatedDto = modelMapper.map(updated, SubjectDto.class);
        updatedDto.setClassName(classEntity.getClassName());

        return updatedDto;
    }

    @Override
    public List<SubjectDto> addSubjectsByClass(SubjectsBulkDto bulkDto)
    {
        ClassEntity classEntity = classEntityRepository.findById(bulkDto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + bulkDto.getClassId()));

        List<SubjectDto> createdSubjects = new ArrayList<>();

        for (String subjectName : bulkDto.getSubjectNames())
        {
            if (subjectRepository.existsBySubjectNameIgnoreCaseAndClassEntity_Id(subjectName, bulkDto.getClassId()))
            {
                // Skip existing subject or you can throw exception
                continue;
            }

            Subject subject = new Subject();
            subject.setSubjectName(subjectName);
            subject.setClassEntity(classEntity);

            Subject saved = subjectRepository.save(subject);

            SubjectDto dto = modelMapper.map(saved, SubjectDto.class);
            dto.setClassName(classEntity.getClassName());

            createdSubjects.add(dto);
        }

        return createdSubjects;
    }

    public List<SubjectDto> getSubjectsByClassId(Long classId)
    {
        List<Subject> subjects = subjectRepository.findByClassEntity_Id(classId);

        if (subjects.isEmpty())
        {
            throw new ResourceNotFoundException("No subjects found for class ID: " + classId);
        }

        // Convert the list of Subject entities to SubjectDto
        return subjects.stream()
                .map(subject -> modelMapper.map(subject, SubjectDto.class))
                .toList();
    }
}