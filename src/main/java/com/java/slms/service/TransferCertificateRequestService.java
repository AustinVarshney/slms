package com.java.slms.service;

import com.java.slms.dto.TCReasonDto;
import com.java.slms.dto.TransferCertificateRequestDto;
import com.java.slms.util.RequestStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TransferCertificateRequestService
{
    @Transactional
    TransferCertificateRequestDto createTransferCertificateRequest(String studentPan, TCReasonDto tcReasonDto);

    List<TransferCertificateRequestDto> getAllRequestsByStudentPan(String studentPan);

    @Transactional
    TransferCertificateRequestDto processRequestDecision(Long requestId, String adminReply, RequestStatus decision);

    @Transactional(readOnly = true)
    List<TransferCertificateRequestDto> getAllRequests(RequestStatus status);
}
