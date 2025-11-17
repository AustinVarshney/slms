package com.java.slms.repository;

import com.java.slms.model.GradeDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeDistributionRepository extends JpaRepository<GradeDistribution, Long>
{
    List<GradeDistribution> findBySchoolIdOrderByMinPercentageDesc(Long schoolId);
    
    Optional<GradeDistribution> findBySchoolIdAndGrade(Long schoolId, String grade);
    
    boolean existsBySchoolId(Long schoolId);
}
