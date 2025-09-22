package com.java.slms.repository;

import com.java.slms.model.Student;
import com.java.slms.model.Teacher;
import com.java.slms.model.TransferCertificateRequest;
import com.java.slms.util.RequestStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferCertificateRequestRepository extends JpaRepository<TransferCertificateRequest, Long>
{
    boolean existsByStudentAndStatusIn(Student student, List<RequestStatus> statuses);

    List<TransferCertificateRequest> findByStudent(Student student);

    Optional<TransferCertificateRequest> findByStudentAndStatus(Student student, RequestStatus status);

    List<TransferCertificateRequest> findAllByStatus(RequestStatus status, Sort sort);

    List<TransferCertificateRequest> findAll(Sort sort);

    List<TransferCertificateRequest> findByApprovedByClassTeacherAndStatus(Teacher teacher, RequestStatus status);

}
