package com.java.slms.serviceImpl;

import com.java.slms.dto.FeeCatalogDto;
import com.java.slms.dto.FeeRequestDTO;
import com.java.slms.dto.MonthlyFeeDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.Fee;
import com.java.slms.model.FeeStructure;
import com.java.slms.model.Student;
import com.java.slms.repository.FeeRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.service.FeeService;
import com.java.slms.util.EntityFetcher;
import com.java.slms.util.EntityNames;
import com.java.slms.util.FeeStatus;
import com.java.slms.util.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeServiceImpl implements FeeService
{

    private final StudentRepository studentRepository;
    private final FeeRepository feeRepository;

    @Override
    public void payFeesOfStudent(FeeRequestDTO feeRequestDTO)
    {
        Student student = EntityFetcher.fetchByIdOrThrow(studentRepository, feeRequestDTO.getStudentPanNumber(), EntityNames.STUDENT);
        FeeStructure feeStructure = student.getCurrentClass().getFeeStructures();
        if (!student.getSession().getId().equals(feeStructure.getSession().getId()))
        {
            throw new WrongArgumentException("Student and FeeStructure session mismatch");
        }
        List<Fee> fees = feeRepository.findByStudent_PanNumberAndMonth(feeRequestDTO.getStudentPanNumber(), feeRequestDTO.getMonth());
        if (fees.isEmpty())
        {
            throw new ResourceNotFoundException("Fee entry not found for given student and month");
        }
        if (fees.size() > 1)
        {
            throw new WrongArgumentException("More than one fee entry found for student and month");
        }
        Fee fee = fees.get(0);

        if (Double.compare(feeStructure.getFeesAmount(), feeRequestDTO.getAmount()) != 0)
        {
            throw new WrongArgumentException("Payment amount does not match the required fee amount.");
        }

        fee.setAmount(fee.getAmount());
        fee.setFeeStructure(feeStructure);
        fee.setStatus(FeeStatus.PAID);
        fee.setStudent(student);
        fee.setPaymentDate(LocalDate.now());
        fee.setReceiptNumber(feeRequestDTO.getReceiptNumber());
        feeRepository.save(fee);
    }

    @Override
    public List<FeeCatalogDto> getAllFeeCatalogs()
    {
        List<Student> students = studentRepository.findAll();
        List<FeeCatalogDto> feeCatalogs = new ArrayList<>();

        for (Student student : students)
        {
            List<Fee> fees = feeRepository.findByStudent_PanNumberOrderByYearAscMonthAsc(student.getPanNumber());

            FeeCatalogDto catalog = new FeeCatalogDto();
            catalog.setStudentId(student.getPanNumber());

            List<MonthlyFeeDto> monthlyFees = new ArrayList<>();
            double totalAmount = 0;
            double totalPaid = 0;
            double totalPending = 0;
            double totalOverdue = 0;

            for (Fee fee : fees)
            {
                MonthlyFeeDto mFee = new MonthlyFeeDto();
                mFee.setMonth(fee.getMonth().name());
                mFee.setYear(fee.getYear());
                mFee.setAmount(fee.getAmount());
                mFee.setDueDate(fee.getDueDate());
                mFee.setStatus(fee.getStatus().name());
                mFee.setPaymentDate(fee.getPaymentDate());
                mFee.setReceiptNumber(fee.getReceiptNumber());

                monthlyFees.add(mFee);

                totalAmount += fee.getAmount();
                switch (fee.getStatus())
                {
                    case PAID:
                        totalPaid += fee.getAmount();
                        break;
                    case PENDING:
                        totalPending += fee.getAmount();
                        break;
                    case OVERDUE:
                        totalOverdue += fee.getAmount();
                        break;
                    default:
                }
            }

            catalog.setMonthlyFees(monthlyFees);
            catalog.setTotalAmount(totalAmount);
            catalog.setTotalPaid(totalPaid);
            catalog.setTotalPending(totalPending);
            catalog.setTotalOverdue(totalOverdue);

            feeCatalogs.add(catalog);
        }

        return feeCatalogs;
    }

}