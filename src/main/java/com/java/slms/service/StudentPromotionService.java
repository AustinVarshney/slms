package com.java.slms.service;

import com.java.slms.dto.PromotionAssignmentRequest;
import com.java.slms.dto.StudentPromotionDto;
import com.java.slms.model.Teacher;

import java.util.List;

public interface StudentPromotionService {

    /**
     * Assign promotion for a student by class teacher
     */
    StudentPromotionDto assignPromotion(Teacher teacher, PromotionAssignmentRequest request, Long schoolId);

    /**
     * Get all promotion assignments for a class
     */
    List<StudentPromotionDto> getPromotionsByClass(Long classId, Long sessionId, Long schoolId);

    /**
     * Get promotion assignment for a specific student
     */
    StudentPromotionDto getPromotionByStudent(String studentPan, Long sessionId, Long schoolId);

    /**
     * Execute promotions for session change (Admin only)
     */
    void executePromotions(Long fromSessionId, Long toSessionId, Long schoolId);

    /**
     * Delete a promotion assignment
     */
    void deletePromotion(Long promotionId, Long schoolId);

    /**
     * Get all pending promotions for a session
     */
    List<StudentPromotionDto> getPendingPromotions(Long sessionId, Long schoolId);

    /**
     * Get all promotions for a session regardless of status
     */
    List<StudentPromotionDto> getPromotionsBySession(Long sessionId, Long schoolId);
}
