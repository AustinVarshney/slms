package com.java.slms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "period_settings")
public class PeriodSettings extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer periodDuration; // in minutes (30-120)

    @Column(nullable = false)
    private LocalTime schoolStartTime; // HH:MM format

    @Column(nullable = false)
    private Integer lunchPeriod; // after which period (1-8)

    @Column(nullable = false)
    private Integer lunchDuration; // in minutes (20-120)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;
}
