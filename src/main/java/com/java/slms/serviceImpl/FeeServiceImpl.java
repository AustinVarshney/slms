package com.java.slms.serviceImpl;

import com.java.slms.dto.FeeCatalogDto;
import com.java.slms.dto.FeeRequestDTO;
import com.java.slms.dto.MonthlyFeeDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.Fee;
import com.java.slms.model.FeeStructure;
import com.java.slms.model.Session;
import com.java.slms.model.Student;
import com.java.slms.repository.FeeRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.service.FeeService;
import com.java.slms.util.EntityFetcher;
import com.java.slms.util.EntityNames;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeServiceImpl implements FeeService
{

    private final StudentRepository studentRepository;
    private final FeeRepository feeRepository;
    private final SessionRepository sessionRepository;

    @Override
    public void payFeesOfStudent(FeeRequestDTO feeRequestDTO)
    {
        Student student = EntityFetcher.fetchByIdOrThrow(studentRepository, feeRequestDTO.getStudentPanNumber(), EntityNames.STUDENT);
        FeeStructure feeStructure = student.getCurrentClass().getFeeStructures();
        Session activeSession = getActiveSession();

        if (!student.getSession().getId().equals(activeSession.getId()))
        {
            throw new WrongArgumentException("Student does not belong to the active session");
        }

        if (!feeStructure.getSession().getId().equals(activeSession.getId()))
        {
            throw new WrongArgumentException("FeeStructure does not belong to the active session");
        }

        List<Fee> fees = feeRepository.findByStudent_PanNumberAndMonth(
                feeRequestDTO.getStudentPanNumber(), feeRequestDTO.getMonth()
        );

        if (fees.isEmpty())
        {
            throw new ResourceNotFoundException("Fee entry not found for given student and month");
        }
        if (fees.size() > 1)
        {
            throw new WrongArgumentException("More than one fee entry found for student and month");
        }

        Fee fee = fees.get(0);

        if (FeeStatus.PAID.equals(fee.getStatus()))
        {
            throw new AlreadyExistException("Fee for this student and month is already paid.");
        }

        if (Double.compare(feeStructure.getFeesAmount(), feeRequestDTO.getAmount()) != 0)
        {
            throw new WrongArgumentException("Payment amount does not match the required fee amount.");
        }

        fee.setFeeStructure(feeStructure);
        fee.setStatus(FeeStatus.PAID);
        fee.setStudent(student);
        fee.setPaymentDate(LocalDate.now());
        fee.setReceiptNumber(feeRequestDTO.getReceiptNumber());

        feeRepository.save(fee);
    }

    @Override
    public List<FeeCatalogDto> getAllFeeCatalogsInActiveSesssion()
    {
        Session activeSession = getActiveSession();
        List<Student> students = studentRepository.findBySession_Id(activeSession.getId());

        return students.stream()
                .map(this::buildCatalogForStudent)
                .collect(Collectors.toList());
    }

    @Override
    public FeeCatalogDto getFeeCatalogByStudentPanNumber(String panNumber)
    {
        Student student = studentRepository.findById(panNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: " + panNumber));

        return buildCatalogForStudent(student);
    }

    private FeeCatalogDto buildCatalogForStudent(Student student)
    {
        List<Fee> fees = feeRepository.findByStudent_PanNumberOrderByYearAscMonthAsc(student.getPanNumber());

        Map<FeeMonth, Fee> feeMap = fees.stream()
                .collect(Collectors.toMap(Fee::getMonth, Function.identity(), (existing, replacement) -> existing));

        FeeCatalogDto catalog = new FeeCatalogDto();
        catalog.setStudentId(student.getPanNumber());

        List<MonthlyFeeDto> monthlyFees = new ArrayList<>();
        double totalAmount = 0;
        double totalPaid = 0;
        double totalPending = 0;
        double totalOverdue = 0;

        FeeMonth sessionStartMonth = Optional.ofNullable(student.getSession())
                .map(Session::getStartMonth)
                .orElse(FeeMonth.JANUARY);

        FeeMonth[] allMonths = FeeMonth.values();
        int startIndex = sessionStartMonth.ordinal();

        for (int i = 0; i < allMonths.length; i++)
        {
            FeeMonth month = allMonths[(startIndex + i) % allMonths.length];
            Fee fee = feeMap.get(month);

            if (fee != null)
            {
                MonthlyFeeDto mFee = new MonthlyFeeDto();
                mFee.setMonth(fee.getMonth().name());
                mFee.setYear(fee.getYear());
                mFee.setAmount(fee.getAmount());
                mFee.setDueDate(fee.getDueDate());
                mFee.setStatus(fee.getStatus().name().toLowerCase());
                mFee.setPaymentDate(fee.getPaymentDate());
                mFee.setReceiptNumber(fee.getReceiptNumber());

                monthlyFees.add(mFee);
                totalAmount += fee.getAmount();

                switch (fee.getStatus())
                {
                    case PAID -> totalPaid += fee.getAmount();
                    case PENDING -> totalPending += fee.getAmount();
                    case OVERDUE -> totalOverdue += fee.getAmount();
                    case UNPAID -> totalPending += fee.getAmount(); // Treat UNPAID as PENDING
                }
            }
        }

        catalog.setMonthlyFees(monthlyFees);
        catalog.setTotalAmount(totalAmount);
        catalog.setTotalPaid(totalPaid);
        catalog.setTotalPending(totalPending);
        catalog.setTotalOverdue(totalOverdue);

        return catalog;
    }

    @Transactional
    @Override
    public void markPendingFeesAsOverdue()
    {
        LocalDate today = LocalDate.now();
        Session activeSession = getActiveSession();

        List<Fee> overdueFees = feeRepository
                .findByStatusAndDueDateLessThanEqualAndFeeStructure_Session_Id(
                        FeeStatus.PENDING, today, activeSession.getId()
                );

        overdueFees.forEach(fee -> fee.setStatus(FeeStatus.OVERDUE));
        feeRepository.saveAll(overdueFees); // Batch update

        log.info("Marked {} pending fees as OVERDUE as of {}", overdueFees.size(), today);
    }

    // Helper
    private Session getActiveSession()
    {
        return sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));
    }
}
