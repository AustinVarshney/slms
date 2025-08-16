package com.java.slms.serviceImpl;

import com.java.slms.dto.FeeRequestDTO;
import com.java.slms.dto.FeeResponseDTO;
import com.java.slms.dto.StudentDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.Fee;
import com.java.slms.model.FeeStructure;
import com.java.slms.model.Student;
import com.java.slms.repository.FeeRepository;
import com.java.slms.repository.FeeStructureRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.service.FeeService;
import com.java.slms.util.CommonUtil;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeServiceImpl implements FeeService
{

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final ModelMapper modelMapper;

    @Override
    public FeeResponseDTO createFee(FeeRequestDTO dto)
    {
        // Fetch Student
        Student student = studentRepository.findById(dto.getStudentPanNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + dto.getStudentPanNumber()));

        // Validate class relation
        Long studentClassId = student.getCurrentClass().getId();

        FeeStructure feeStructure = feeStructureRepository.findById(dto.getFeeStructureId())
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure not found: " + dto.getFeeStructureId()));

        Long feeStructureClassId = feeStructure.getClassEntity().getId();
        if (!studentClassId.equals(feeStructureClassId))
        {
            throw new WrongArgumentException("FeeStructure does not belong to the student's class.");
        }

        // Check if already paid for this month
        boolean alreadyPaid = feeRepository
                .existsByFeeStructureIdAndStudent_PanNumberAndMonth(dto.getFeeStructureId(), dto.getStudentPanNumber(), dto.getMonth());

        if (alreadyPaid)
        {
            throw new WrongArgumentException("Fee already paid for month: " + dto.getMonth());
        }

        // Validate exact amount match
        if (dto.getAmountPaid() == null || dto.getAmountPaid() <= 0)
        {
            throw new WrongArgumentException("Amount paid must be greater than zero.");
        }
        if (!dto.getAmountPaid().equals(feeStructure.getDefaultAmount()))
        {
            throw new WrongArgumentException("Amount paid must exactly match the monthly fee: " + feeStructure.getDefaultAmount());
        }

        // Save Fee record
        Fee fee = new Fee();
        fee.setStudent(student);
        fee.setFeeStructure(feeStructure);
        fee.setMonth(dto.getMonth());
        fee.setTotalAmount(feeStructure.getDefaultAmount());
        fee.setAmountPaid(dto.getAmountPaid());
        fee.setRemainingAmount(0.0);
        fee.setStatus(FeeStatus.PAID);
        fee.setPaidOn(new Date());
        fee.setPaymentHistory(dto.getPaymentHistory());

        Fee saved = feeRepository.save(fee);

        return modelMapper.map(saved, FeeResponseDTO.class);
    }

    @Override
    public List<FeeResponseDTO> getFeesByStudentPan(String panNumber) {
        Student student = CommonUtil.fetchStudentByPan(studentRepository, panNumber);
        List<Fee> paidFees = feeRepository.findByStudent_PanNumber(panNumber);
        return buildFeeResponseList(student, paidFees);
    }

    @Override
    public List<FeeResponseDTO> getFeesByStudentPan(String panNumber, FeeMonth month) {
        if (!studentRepository.existsById(panNumber)) {
            log.error("Student not exists with PAN: {}", panNumber);
            throw new ResourceNotFoundException("Student not exists with PAN: " + panNumber);
        }

        Student student = CommonUtil.fetchStudentByPan(studentRepository, panNumber);
        List<Fee> paidFees = feeRepository.findByStudent_PanNumberAndMonth(panNumber, month);
        return buildFeeResponseList(student, paidFees);
    }

    @Override
    public List<FeeResponseDTO> getFeesByFeeStructureId(Long feeStructureId)
    {
        if (!feeStructureRepository.existsById(feeStructureId))
        {
            throw new ResourceNotFoundException("FeeStructure not found with ID: " + feeStructureId);
        }

        return feeRepository.findByFeeStructure_Id(feeStructureId)
                .stream()
                .map(f -> modelMapper.map(f, FeeResponseDTO.class))
                .toList();
    }

    @Override
    public List<FeeResponseDTO> getFeesByFeeStructureId(Long feeStructureId, FeeMonth month)
    {
        if (!feeStructureRepository.existsById(feeStructureId))
        {
            throw new ResourceNotFoundException("FeeStructure not found with ID: " + feeStructureId);
        }

        return feeRepository.findByFeeStructure_IdAndMonth(feeStructureId, month)
                .stream()
                .map(f -> modelMapper.map(f, FeeResponseDTO.class))
                .toList();
    }


    @Override
    public List<FeeResponseDTO> getFeesByStatus(FeeStatus status)
    {
        return feeRepository.findByStatus(status)
                .stream()
                .map(f -> modelMapper.map(f, FeeResponseDTO.class))
                .toList();
    }

    @Override
    public List<FeeResponseDTO> getFeesByStatus(FeeStatus status, FeeMonth month)
    {
        return feeRepository.findByMonthAndStatus(month, status)
                .stream()
                .map(f -> modelMapper.map(f, FeeResponseDTO.class))
                .toList();
    }

    @Override
    public List<FeeResponseDTO> getFeesPaidBetween(Date startDate, Date endDate)
    {
        return feeRepository.findByPaidOnBetween(startDate, endDate)
                .stream()
                .map(f -> modelMapper.map(f, FeeResponseDTO.class))
                .toList();
    }

    @Override
    public List<FeeResponseDTO> getFeesByStudentPanAndStatus(String panNumber, FeeStatus status)
    {
        if (!studentRepository.existsById(panNumber))
        {
            log.error("Student not exists with PAN: {}", panNumber);
            throw new ResourceNotFoundException("Student not exists with PAN: " + panNumber);
        }

        return feeRepository.findByStudent_PanNumberAndStatus(panNumber, status)
                .stream()
                .map(f -> modelMapper.map(f, FeeResponseDTO.class))
                .toList();
    }

    @Override
    public List<FeeResponseDTO> getFeesByStudentPanAndStatus(String panNumber, FeeStatus status, FeeMonth month)
    {
        if (!studentRepository.existsById(panNumber))
        {
            log.error("Student not exists with PAN: {}", panNumber);
            throw new ResourceNotFoundException("Student not exists with PAN: " + panNumber);
        }

        return feeRepository.findByStudent_PanNumberAndStatusAndMonth(panNumber, status, month)
                .stream()
                .map(f -> modelMapper.map(f, FeeResponseDTO.class))
                .toList();
    }

    @Override
    public List<StudentDto> getDefaulters(Long feeStructureId, FeeMonth month)
    {
        // 1️⃣ Validate FeeStructure
        FeeStructure feeStructure = feeStructureRepository.findById(feeStructureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FeeStructure not found with ID: " + feeStructureId));

        Long classId = feeStructure.getClassEntity().getId();

        // 2️⃣ Get all students in that class
        List<Student> studentsInClass = studentRepository.findByCurrentClass_Id(classId);

        // 3️⃣ Get students who paid for that month
        List<String> paidPanNumbers = feeRepository
                .findByFeeStructure_IdAndMonth(feeStructureId, month)
                .stream()
                .map(f -> f.getStudent().getPanNumber())
                .toList();

        // 4️⃣ Filter out those who haven't paid
        return studentsInClass.stream()
                .filter(s -> !paidPanNumbers.contains(s.getPanNumber()))
                .map(s -> modelMapper.map(s, StudentDto.class))
                .toList();
    }

    private List<FeeResponseDTO> buildFeeResponseList(Student student, List<Fee> paidFees) {
        List<FeeStructure> feeStructures = student.getCurrentClass().getFeeStructures();

        // Extract paid FeeStructure IDs
        Set<Long> paidFeeStructureIds = paidFees.stream()
                .map(f -> f.getFeeStructure().getId())
                .collect(Collectors.toSet());

        // Start with paid fee DTOs
        List<FeeResponseDTO> feeResponseDTOs = paidFees.stream()
                .map(f -> modelMapper.map(f, FeeResponseDTO.class))
                .collect(Collectors.toList());

        // Add unpaid fee DTOs for missing FeeStructure IDs
        feeStructures.stream()
                .filter(feeStructure -> !paidFeeStructureIds.contains(feeStructure.getId()))
                .forEach(feeStructure -> {
                    FeeResponseDTO unpaidFeeDTO = new FeeResponseDTO();
                    unpaidFeeDTO.setFeeStructureId(feeStructure.getId());
                    unpaidFeeDTO.setStudentPanNumber(student.getPanNumber());
                    unpaidFeeDTO.setStudentName(student.getName());
                    unpaidFeeDTO.setFeeType(feeStructure.getFeeType());
                    unpaidFeeDTO.setTotalAmount(feeStructure.getDefaultAmount());
                    unpaidFeeDTO.setAmountPaid(0.0);
                    unpaidFeeDTO.setRemainingAmount(feeStructure.getDefaultAmount());
                    unpaidFeeDTO.setStatus(FeeStatus.UNPAID);
                    unpaidFeeDTO.setPaidOn(null);
                    feeResponseDTOs.add(unpaidFeeDTO);
                });

        return feeResponseDTOs;
    }



}