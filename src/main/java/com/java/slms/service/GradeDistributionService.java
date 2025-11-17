package com.java.slms.service;

import com.java.slms.dto.GradeDistributionDto;

import java.util.List;

public interface GradeDistributionService
{
    /**
     * Get grade distribution for a school
     */
    List<GradeDistributionDto> getGradeDistribution(Long schoolId);
    
    /**
     * Set custom grade distribution for a school
     */
    List<GradeDistributionDto> setGradeDistribution(Long schoolId, List<GradeDistributionDto> gradeDistributions);
    
    /**
     * Calculate grade based on percentage for a school
     * Uses custom grade distribution if available, otherwise uses default
     */
    String calculateGrade(Long schoolId, Double percentage);
    
    /**
     * Reset to default grade distribution
     */
    List<GradeDistributionDto> resetToDefault(Long schoolId);
    
    /**
     * Get default grade distribution
     */
    List<GradeDistributionDto> getDefaultGradeDistribution();
}
