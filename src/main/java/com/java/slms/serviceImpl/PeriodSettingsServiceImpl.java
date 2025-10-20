package com.java.slms.serviceImpl;

import com.java.slms.dto.PeriodSettingsDTO;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.PeriodSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodSettingsServiceImpl implements PeriodSettingsService {
    
    private final PeriodSettingsRepository periodSettingsRepository;
    private final SchoolRepository schoolRepository;
    private final SessionRepository sessionRepository;
    private final TimetableRepository timetableRepository;
    
    @Override
    public PeriodSettingsDTO getPeriodSettings(Long schoolId) {
        log.info("Fetching period settings for school ID: {}", schoolId);
        
        // Get school
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        
        // Get active session
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new RuntimeException("No active session found"));
        
        // Try to find existing settings for this school and session
        PeriodSettings settings = periodSettingsRepository
                .findBySchoolIdAndSessionId(schoolId, activeSession.getId())
                .orElse(null);
        
        // If no settings found, return default values
        if (settings == null) {
            log.info("No period settings found, returning defaults");
            PeriodSettingsDTO defaultSettings = new PeriodSettingsDTO();
            defaultSettings.setPeriodDuration(60);
            defaultSettings.setSchoolStartTime("08:00");
            defaultSettings.setLunchPeriod(4);
            defaultSettings.setLunchDuration(30);
            defaultSettings.setSchoolId(schoolId);
            defaultSettings.setSessionId(activeSession.getId());
            return defaultSettings;
        }
        
        // Convert to DTO
        return convertToDTO(settings);
    }
    
    @Override
    @Transactional
    public PeriodSettingsDTO savePeriodSettings(PeriodSettingsDTO settingsDTO, Long schoolId) {
        log.info("Saving period settings for school ID: {}", schoolId);
        
        // Validate input
        validateSettings(settingsDTO);
        
        // Get school
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        
        // Get active session
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new RuntimeException("No active session found"));
        
        // Find existing settings or create new
        PeriodSettings settings = periodSettingsRepository
                .findBySchoolIdAndSessionId(schoolId, activeSession.getId())
                .orElse(new PeriodSettings());
        
        // Update settings
        settings.setPeriodDuration(settingsDTO.getPeriodDuration());
        settings.setSchoolStartTime(LocalTime.parse(settingsDTO.getSchoolStartTime()));
        settings.setLunchPeriod(settingsDTO.getLunchPeriod());
        settings.setLunchDuration(settingsDTO.getLunchDuration());
        settings.setSchool(school);
        settings.setSession(activeSession);
        
        // Save settings
        settings = periodSettingsRepository.save(settings);
        log.info("Period settings saved with ID: {}", settings.getId());
        
        // Recalculate all timetable entries
        recalculateTimetables(schoolId, activeSession.getId());
        
        return convertToDTO(settings);
    }
    
    @Override
    @Transactional
    public void recalculateTimetables(Long schoolId, Long sessionId) {
        log.info("Recalculating timetables for school ID: {} and session ID: {}", schoolId, sessionId);
        
        // Get period settings
        PeriodSettings settings = periodSettingsRepository
                .findBySchoolIdAndSessionId(schoolId, sessionId)
                .orElse(null);
        
        if (settings == null) {
            log.warn("No period settings found, skipping timetable recalculation");
            return;
        }
        
        // Get all timetable entries for this school and session
        List<TimeTable> timetables = timetableRepository
                .findBySchoolIdAndSessionId(schoolId, sessionId);
        
        log.info("Found {} timetable entries to update", timetables.size());
        
        // Recalculate times for each entry based on period number
        for (TimeTable timetable : timetables) {
            int period = timetable.getPeriod();
            
            // Calculate start and end time based on period number and settings
            LocalTime[] times = calculatePeriodTimes(period, settings);
            timetable.setStartTime(times[0]);
            timetable.setEndTime(times[1]);
        }
        
        // Save all updated timetables
        timetableRepository.saveAll(timetables);
        log.info("Successfully updated {} timetable entries", timetables.size());
    }
    
    /**
     * Calculate start and end time for a given period
     */
    private LocalTime[] calculatePeriodTimes(int period, PeriodSettings settings) {
        LocalTime currentTime = settings.getSchoolStartTime();
        
        // Calculate time for each period before this one
        for (int i = 1; i < period; i++) {
            // Add period duration
            currentTime = currentTime.plusMinutes(settings.getPeriodDuration());
            
            // Add lunch duration if lunch comes after this period
            if (i == settings.getLunchPeriod()) {
                currentTime = currentTime.plusMinutes(settings.getLunchDuration());
            }
        }
        
        // Current period start time
        LocalTime startTime = currentTime;
        LocalTime endTime = currentTime.plusMinutes(settings.getPeriodDuration());
        
        return new LocalTime[]{startTime, endTime};
    }
    
    /**
     * Validate period settings
     */
    private void validateSettings(PeriodSettingsDTO settingsDTO) {
        if (settingsDTO.getPeriodDuration() == null || 
            settingsDTO.getPeriodDuration() < 30 || 
            settingsDTO.getPeriodDuration() > 120) {
            throw new IllegalArgumentException("Period duration must be between 30 and 120 minutes");
        }
        
        if (settingsDTO.getSchoolStartTime() == null || 
            !settingsDTO.getSchoolStartTime().matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("Invalid school start time format (expected HH:MM)");
        }
        
        if (settingsDTO.getLunchPeriod() == null || 
            settingsDTO.getLunchPeriod() < 1 || 
            settingsDTO.getLunchPeriod() > 8) {
            throw new IllegalArgumentException("Lunch period must be between 1 and 8");
        }
        
        if (settingsDTO.getLunchDuration() == null || 
            settingsDTO.getLunchDuration() < 20 || 
            settingsDTO.getLunchDuration() > 120) {
            throw new IllegalArgumentException("Lunch duration must be between 20 and 120 minutes");
        }
    }
    
    /**
     * Convert entity to DTO
     */
    private PeriodSettingsDTO convertToDTO(PeriodSettings settings) {
        PeriodSettingsDTO dto = new PeriodSettingsDTO();
        dto.setId(settings.getId());
        dto.setPeriodDuration(settings.getPeriodDuration());
        dto.setSchoolStartTime(settings.getSchoolStartTime().toString());
        dto.setLunchPeriod(settings.getLunchPeriod());
        dto.setLunchDuration(settings.getLunchDuration());
        dto.setSchoolId(settings.getSchool().getId());
        dto.setSessionId(settings.getSession() != null ? settings.getSession().getId() : null);
        return dto;
    }
}
