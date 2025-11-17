package com.java.slms.serviceImpl;

import com.java.slms.dto.GradeDistributionDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.GradeDistribution;
import com.java.slms.model.School;
import com.java.slms.repository.GradeDistributionRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.service.GradeDistributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeDistributionServiceImpl implements GradeDistributionService
{
    private final GradeDistributionRepository gradeDistributionRepository;
    private final SchoolRepository schoolRepository;

    @Override
    public List<GradeDistributionDto> getGradeDistribution(Long schoolId)
    {
        log.info("Fetching grade distribution for school: {}", schoolId);
        
        List<GradeDistribution> distributions = gradeDistributionRepository
                .findBySchoolIdOrderByMinPercentageDesc(schoolId);
        
        if (distributions.isEmpty())
        {
            log.info("No custom grade distribution found for school {}. Returning default.", schoolId);
            return getDefaultGradeDistribution();
        }
        
        return distributions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GradeDistributionDto> setGradeDistribution(Long schoolId, List<GradeDistributionDto> gradeDistributions)
    {
        log.info("Setting custom grade distribution for school: {}", schoolId);
        
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
        
        // Delete existing distributions
        List<GradeDistribution> existing = gradeDistributionRepository
                .findBySchoolIdOrderByMinPercentageDesc(schoolId);
        gradeDistributionRepository.deleteAll(existing);
        
        // Create new distributions
        List<GradeDistribution> newDistributions = gradeDistributions.stream()
                .map(dto -> {
                    GradeDistribution gd = new GradeDistribution();
                    gd.setSchool(school);
                    gd.setGrade(dto.getGrade());
                    gd.setMinPercentage(dto.getMinPercentage());
                    gd.setMaxPercentage(dto.getMaxPercentage());
                    gd.setDescription(dto.getDescription());
                    return gd;
                })
                .collect(Collectors.toList());
        
        List<GradeDistribution> saved = gradeDistributionRepository.saveAll(newDistributions);
        
        return saved.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public String calculateGrade(Long schoolId, Double percentage)
    {
        if (percentage == null || percentage < 0)
        {
            return "N/A";
        }
        
        List<GradeDistribution> distributions = gradeDistributionRepository
                .findBySchoolIdOrderByMinPercentageDesc(schoolId);
        
        if (distributions.isEmpty())
        {
            // Use default grading system
            return calculateDefaultGrade(percentage);
        }
        
        // Find matching grade
        for (GradeDistribution dist : distributions)
        {
            if (percentage >= dist.getMinPercentage() && percentage <= dist.getMaxPercentage())
            {
                return dist.getGrade();
            }
        }
        
        return "F"; // Default fallback
    }

    @Override
    @Transactional
    public List<GradeDistributionDto> resetToDefault(Long schoolId)
    {
        log.info("Resetting to default grade distribution for school: {}", schoolId);
        
        // Delete existing custom distributions
        List<GradeDistribution> existing = gradeDistributionRepository
                .findBySchoolIdOrderByMinPercentageDesc(schoolId);
        gradeDistributionRepository.deleteAll(existing);
        
        return getDefaultGradeDistribution();
    }

    @Override
    public List<GradeDistributionDto> getDefaultGradeDistribution()
    {
        List<GradeDistributionDto> defaultGrades = new ArrayList<>();
        
        defaultGrades.add(GradeDistributionDto.builder()
                .grade("A+")
                .minPercentage(90.0)
                .maxPercentage(100.0)
                .description("Outstanding")
                .build());
        
        defaultGrades.add(GradeDistributionDto.builder()
                .grade("A")
                .minPercentage(80.0)
                .maxPercentage(89.99)
                .description("Excellent")
                .build());
        
        defaultGrades.add(GradeDistributionDto.builder()
                .grade("B+")
                .minPercentage(70.0)
                .maxPercentage(79.99)
                .description("Very Good")
                .build());
        
        defaultGrades.add(GradeDistributionDto.builder()
                .grade("B")
                .minPercentage(60.0)
                .maxPercentage(69.99)
                .description("Good")
                .build());
        
        defaultGrades.add(GradeDistributionDto.builder()
                .grade("C")
                .minPercentage(50.0)
                .maxPercentage(59.99)
                .description("Average")
                .build());
        
        defaultGrades.add(GradeDistributionDto.builder()
                .grade("D")
                .minPercentage(40.0)
                .maxPercentage(49.99)
                .description("Below Average")
                .build());
        
        defaultGrades.add(GradeDistributionDto.builder()
                .grade("E")
                .minPercentage(33.0)
                .maxPercentage(39.99)
                .description("Poor")
                .build());
        
        defaultGrades.add(GradeDistributionDto.builder()
                .grade("F")
                .minPercentage(0.0)
                .maxPercentage(32.99)
                .description("Fail")
                .build());
        
        return defaultGrades;
    }

    private String calculateDefaultGrade(Double percentage)
    {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        if (percentage >= 33) return "E";
        return "F";
    }

    private GradeDistributionDto toDto(GradeDistribution entity)
    {
        return GradeDistributionDto.builder()
                .id(entity.getId())
                .grade(entity.getGrade())
                .minPercentage(entity.getMinPercentage())
                .maxPercentage(entity.getMaxPercentage())
                .description(entity.getDescription())
                .build();
    }
}
