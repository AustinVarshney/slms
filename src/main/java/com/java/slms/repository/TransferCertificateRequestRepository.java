package com.java.slms.repository;

import com.java.slms.model.Student;
import com.java.slms.model.TransferCertificateRequest;
import com.java.slms.util.RequestStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransferCertificateRequestRepository extends JpaRepository<TransferCertificateRequest, Long>
{
    boolean existsByStudentAndStatusIn(Student student, List<RequestStatus> statuses);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM TransferCertificateRequest t " +
            "WHERE t.student = :student " +
            "AND t.status IN :statuses " +
            "AND t.school.id = :schoolId")
    boolean existsByStudentAndStatusInAndSchoolId(
            @Param("student") Student student,
            @Param("statuses") List<RequestStatus> statuses,
            @Param("schoolId") Long schoolId);


    List<TransferCertificateRequest> findByStudent(Student student);

    @Query("SELECT t FROM TransferCertificateRequest t " +
            "WHERE t.student = :student " +
            "AND t.school.id = :schoolId")
    List<TransferCertificateRequest> findByStudentAndSchoolId(
            @Param("student") Student student,
            @Param("schoolId") Long schoolId);

    @Query("SELECT t FROM TransferCertificateRequest t " +
            "WHERE t.id = :requestId AND t.school.id = :schoolId")
    Optional<TransferCertificateRequest> findByIdAndSchoolId(
            @Param("requestId") Long requestId,
            @Param("schoolId") Long schoolId);


    Optional<TransferCertificateRequest> findByStudentAndStatus(Student student, RequestStatus status);

    List<TransferCertificateRequest> findAllByStatus(RequestStatus status, Sort sort);

    List<TransferCertificateRequest> findAll(Sort sort);

    @Query("SELECT t FROM TransferCertificateRequest t " +
            "WHERE t.status = :status " +
            "AND t.school.id = :schoolId " +
            "ORDER BY t.requestDate DESC")
    List<TransferCertificateRequest> findAllByStatusAndSchoolId(
            @Param("status") RequestStatus status,
            @Param("schoolId") Long schoolId);

    @Query("SELECT t FROM TransferCertificateRequest t " +
            "WHERE t.school.id = :schoolId " +
            "ORDER BY t.requestDate DESC")
    List<TransferCertificateRequest> findAllBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT t FROM TransferCertificateRequest t " +
            "WHERE t.school.id = :schoolId " +
            "AND t.status IN ('APPROVED', 'REJECTED') " +
            "AND t.adminActionDate >= :fromDate " +
            "ORDER BY t.adminActionDate DESC")
    List<TransferCertificateRequest> findProcessedRequestsFromDate(
            @Param("schoolId") Long schoolId,
            @Param("fromDate") java.time.LocalDate fromDate);

}
