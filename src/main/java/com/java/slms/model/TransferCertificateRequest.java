package com.java.slms.model;

import com.java.slms.util.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transfer_certificate_request")
public class TransferCertificateRequest extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_pan_number", referencedColumnName = "panNumber")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", referencedColumnName = "id")
    private Admin approvedByAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity lastClass;  // class last attended

    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id", referencedColumnName = "id")
    private Teacher approvedByClassTeacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_teacher_approval_status")
    private RequestStatus classTeacherApprovalStatus;

    @Column(name = "class_teacher_reply_to_admin", columnDefinition = "TEXT")
    private String teacherReplyToAdmin;

    private LocalDate teacherActionDate;

    private String reason;

    @Column(name = "admin_reply_to_student", columnDefinition = "TEXT")
    private String adminReplyToStudent;

    @Column(name = "admin_message_to_teacher", columnDefinition = "TEXT")
    private String adminMessageToTeacher;

    private LocalDate adminActionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

}
