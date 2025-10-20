package com.java.slms.controller;

import com.java.slms.dto.PeriodSettingsDTO;
import com.java.slms.service.PeriodSettingsService;
import com.java.slms.payload.RestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/period-settings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PeriodSettingsController {
    
    private final PeriodSettingsService periodSettingsService;
    
    /**
     * Get period settings for the current school
     */
    @GetMapping
    public ResponseEntity<RestResponse<PeriodSettingsDTO>> getPeriodSettings(
            @RequestAttribute Long schoolId) {
        try {
            log.info("GET /api/period-settings - School ID: {}", schoolId);
            PeriodSettingsDTO settings = periodSettingsService.getPeriodSettings(schoolId);
            return ResponseEntity.ok(
                    RestResponse.<PeriodSettingsDTO>builder()
                            .data(settings)
                            .message("Period settings retrieved successfully")
                            .status(200)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error fetching period settings", e);
            return ResponseEntity.internalServerError()
                    .body(RestResponse.<PeriodSettingsDTO>builder()
                            .data(null)
                            .message(e.getMessage())
                            .status(500)
                            .build());
        }
    }
    
    /**
     * Save or update period settings
     * This will automatically recalculate all timetable entries
     */
    @PostMapping
    public ResponseEntity<RestResponse<PeriodSettingsDTO>> savePeriodSettings(
            @RequestBody PeriodSettingsDTO settingsDTO,
            @RequestAttribute Long schoolId) {
        try {
            log.info("POST /api/period-settings - School ID: {}, Settings: {}", schoolId, settingsDTO);
            PeriodSettingsDTO savedSettings = periodSettingsService.savePeriodSettings(settingsDTO, schoolId);
            return ResponseEntity.ok(
                    RestResponse.<PeriodSettingsDTO>builder()
                            .data(savedSettings)
                            .message("Period settings saved and timetables updated successfully")
                            .status(200)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(RestResponse.<PeriodSettingsDTO>builder()
                            .data(null)
                            .message(e.getMessage())
                            .status(400)
                            .build());
        } catch (Exception e) {
            log.error("Error saving period settings", e);
            return ResponseEntity.internalServerError()
                    .body(RestResponse.<PeriodSettingsDTO>builder()
                            .data(null)
                            .message(e.getMessage())
                            .status(500)
                            .build());
        }
    }
}
