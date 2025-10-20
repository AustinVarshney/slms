package com.java.slms.service;

import com.java.slms.dto.PeriodSettingsDTO;

public interface PeriodSettingsService {
    
    /**
     * Get period settings for the current school and active session
     */
    PeriodSettingsDTO getPeriodSettings(Long schoolId);
    
    /**
     * Save or update period settings
     * This will also recalculate all timetable start/end times
     */
    PeriodSettingsDTO savePeriodSettings(PeriodSettingsDTO settingsDTO, Long schoolId);
    
    /**
     * Recalculate all timetable entries based on new period settings
     */
    void recalculateTimetables(Long schoolId, Long sessionId);
}
