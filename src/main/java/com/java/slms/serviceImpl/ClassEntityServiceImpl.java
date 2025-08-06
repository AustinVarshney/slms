package com.java.slms.serviceImpl;

import com.java.slms.dto.ClassEntityDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.ClassEntity;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.service.ClassEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassEntityServiceImpl implements ClassEntityService
{
    private final ClassEntityRepository classEntityRepository;
    private final ModelMapper modelMapper;

    @Override
    public ClassEntityDto addClass(ClassEntityDto classEntityDto)
    {
        if (classEntityRepository.findByClassNameIgnoreCase(classEntityDto.getClassName()) != null)
        {
            log.info("Class already exists with name: {}", classEntityDto.getClassName());
            throw new AlreadyExistException("Class already exists with name: " + classEntityDto.getClassName());
        }

        ClassEntity classEntity = modelMapper.map(classEntityDto, ClassEntity.class);
        ClassEntity savedEntity = classEntityRepository.save(classEntity);
        ClassEntityDto savedClassDto = modelMapper.map(savedEntity, ClassEntityDto.class);
        if (savedEntity.getStudents() != null && !savedEntity.getStudents().isEmpty())
            savedClassDto.setTotalStudents(savedEntity.getStudents().size());
        return modelMapper.map(savedEntity, ClassEntityDto.class);
    }

    @Override
    public List<ClassEntityDto> getAllClass()
    {
        List<ClassEntity> classEntities = classEntityRepository.findAll();

        return classEntities.stream().map(classEntity ->
        {
            ClassEntityDto dto = modelMapper.map(classEntity, ClassEntityDto.class);

            if (classEntity.getStudents() != null && !classEntity.getStudents().isEmpty())
            {
                dto.setTotalStudents(classEntity.getStudents().size());
            }
            else
            {
                dto.setTotalStudents(0);
            }

            return dto;
        }).toList();
    }

    @Override
    public List<ClassEntityDto> getClassWithSections()
    {
        return List.of();
    }

    @Override
    public ClassEntityDto getClassByClassId(Long id)
    {
        ClassEntity classEntity = classEntityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + id));
        ClassEntityDto dto = modelMapper.map(classEntity, ClassEntityDto.class);
        dto.setTotalStudents(classEntity.getStudents() != null ? classEntity.getStudents().size() : 0);
        return dto;
    }

    public void deleteClassById(Long id)
    {
        ClassEntity classEntity = classEntityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + id));
        classEntityRepository.delete(classEntity);
    }

    @Override
    public ClassEntityDto updateClassNameById(Long id, ClassEntityDto classEntityDto)
    {
        ClassEntity existingClass = classEntityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Class not found with ID: " + id)
        );

        // Check for duplicate name
        ClassEntity duplicateClass = classEntityRepository.findByClassNameIgnoreCase(classEntityDto.getClassName());
        if (duplicateClass != null && !duplicateClass.getId().equals(existingClass.getId()))
        {
            throw new AlreadyExistException("Class already exists with name: " + classEntityDto.getClassName());
        }

        existingClass.setClassName(classEntityDto.getClassName());
        ClassEntity updatedClass = classEntityRepository.save(existingClass);

        ClassEntityDto dto = modelMapper.map(updatedClass, ClassEntityDto.class);
        dto.setTotalStudents(updatedClass.getStudents() != null ? updatedClass.getStudents().size() : 0);
        return dto;
    }

}
