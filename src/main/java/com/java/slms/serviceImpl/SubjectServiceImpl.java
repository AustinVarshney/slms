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

    @Override
    public SubjectDto addSubject(SubjectDto subjectDto)
    {
        if (subjectRepository.existsBySubjectNameIgnoreCase(subjectDto.getSubjectName()))
        {
            throw new AlreadyExistException("Subject already exists: " + subjectDto.getSubjectName());
        }

        ClassEntity classEntity = classEntityRepository.findByClassNameIgnoreCase(subjectDto.getClassName());
        if (classEntity == null)
        {
            throw new ResourceNotFoundException("Class not found: " + subjectDto.getClassName());
        }

        Subject subject = new Subject();
        subject.setSubjectName(subjectDto.getSubjectName());
        subject.setClassEntity(classEntity);

        Subject saved = subjectRepository.save(subject);
        SubjectDto savedDto = modelMapper.map(saved, SubjectDto.class);
        savedDto.setClassName(classEntity.getClassName());

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
    public SubjectDto getSubjectByName(String name)
    {
        Subject subject = subjectRepository.findBySubjectNameIgnoreCase(name);
        if (subject == null)
        {
            throw new ResourceNotFoundException("Subject not found: " + name);
        }
        SubjectDto dto = modelMapper.map(subject, SubjectDto.class);
        dto.setClassName(subject.getClassEntity().getClassName());
        return dto;
    }

    @Override
    public void deleteSubject(String name)
    {
        Subject subject = subjectRepository.findBySubjectNameIgnoreCase(name);
        if (subject == null)
        {
            throw new ResourceNotFoundException("Subject not found: " + name);
        }
        subjectRepository.delete(subject);
    }

    @Override
    public SubjectDto updateSubject(String name, SubjectDto subjectDto)
    {
        Subject existing = subjectRepository.findBySubjectNameIgnoreCase(name);
        if (existing == null)
        {
            throw new ResourceNotFoundException("Subject not found: " + name);
        }

        if (!existing.getSubjectName().equalsIgnoreCase(subjectDto.getSubjectName())
                && subjectRepository.existsBySubjectNameIgnoreCase(subjectDto.getSubjectName()))
        {
            throw new AlreadyExistException("Subject already exists with name: " + subjectDto.getSubjectName());
        }

        ClassEntity classEntity = classEntityRepository.findByClassNameIgnoreCase(subjectDto.getClassName());
        if (classEntity == null)
        {
            throw new ResourceNotFoundException("Class not found: " + subjectDto.getClassName());
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
        ClassEntity classEntity = classEntityRepository.findByClassNameIgnoreCase(bulkDto.getClassName());
        if (classEntity == null)
        {
            throw new ResourceNotFoundException("Class not found: " + bulkDto.getClassName());
        }

        List<SubjectDto> createdSubjects = new ArrayList<>();
        for (String subjectName : bulkDto.getSubjectNames())
        {
            if (subjectRepository.existsBySubjectNameIgnoreCase(subjectName))
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
}