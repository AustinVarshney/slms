package com.java.slms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDto
{
    private Long id;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime date;
    private List<StudentAttendance> studentAttendances;
    private Long classId;
    private String className;
    private Date createdAt;
    private Date updatedAt;
}
