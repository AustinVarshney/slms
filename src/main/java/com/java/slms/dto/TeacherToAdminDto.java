package com.java.slms.dto;

import com.java.slms.util.RequestStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherToAdminDto
{
    private String teacherReplyToAdmin;
    private RequestStatus status;
}
