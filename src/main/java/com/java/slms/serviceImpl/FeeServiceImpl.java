package com.java.slms.serviceImpl;

import com.java.slms.dto.FeeCatalogDto;
import com.java.slms.dto.FeeRequestDTO;
import com.java.slms.dto.MonthlyFeeDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Fee;
import com.java.slms.model.FeeStructure;
import com.java.slms.model.Session;
import com.java.slms.model.Student;
import com.java.slms.repository.FeeRepository;
import com.java.slms.repository.FeeStructureRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.service.FeeService;
import com.java.slms.util.FeeMonth;
import com.java.slms.util.FeeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeServiceImpl implements FeeService
{

    private final StudentRepository studentRepository;
    private final FeeRepository feeRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final SessionRepository sessionRepository;

    @Override
    public void payFeesOfStudent(FeeRequestDTO feeRequestDTO, Long schoolId)
    {
        Student student = studentRepository
                .findByPanNumberIgnoreCaseAndSchool_Id(feeRequestDTO.getStudentPanNumber(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: "
                        + feeRequestDTO.getStudentPanNumber() + " in school ID: " + schoolId));

        Session session = sessionRepository.findBySessionIdAndSchoolId(feeRequestDTO.getSessionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + 
                        feeRequestDTO.getSessionId() + " for schoolId: " + schoolId));

        List<Fee> fees = feeRepository.findFeesByPanNumberAndSchoolIdAndMonth(
                feeRequestDTO.getStudentPanNumber(), schoolId, feeRequestDTO.getMonth(), session.getId()
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

        // Validate payment amount matches the fee amount
        if (Double.compare(fee.getAmount(), feeRequestDTO.getAmount()) != 0)
        {
            throw new WrongArgumentException("Payment amount " + feeRequestDTO.getAmount() + 
                    " does not match the required fee amount " + fee.getAmount());
        }

        // Generate unique receipt number if not provided
        String receiptNumber = feeRequestDTO.getReceiptNumber();
        if (receiptNumber == null || receiptNumber.trim().isEmpty())
        {
            receiptNumber = generateUniqueReceiptNumber(student, session);
        }
        
        // Validate receipt number uniqueness
        if (isReceiptNumberExists(receiptNumber))
        {
            throw new WrongArgumentException("Receipt number " + receiptNumber + " already exists. Please use a unique receipt number.");
        }

        // Update fee to PAID status
        fee.setStatus(FeeStatus.PAID);
        fee.setPaymentDate(LocalDate.now());
        fee.setReceiptNumber(receiptNumber);

        feeRepository.save(fee);
    }
    
    /**
     * Generate unique receipt number format: REC-{SessionYear}-{SchoolId}-{PAN}-{Timestamp}
     * Example: REC-2025-1-ABC123-20251031143022
     */
    private String generateUniqueReceiptNumber(Student student, Session session)
    {
        String sessionYear = String.valueOf(session.getStartDate().getYear());
        String schoolId = String.valueOf(student.getSchool().getId());
        String pan = student.getPanNumber();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        return String.format("REC-%s-%s-%s-%s", sessionYear, schoolId, pan, timestamp);
    }
    
    /**
     * Check if receipt number already exists
     */
    private boolean isReceiptNumberExists(String receiptNumber)
    {
        return feeRepository.existsByReceiptNumber(receiptNumber);
    }

    @Override
    public List<FeeCatalogDto> getAllFeeCatalogsInActiveSession(Long schoolId)
    {
        List<Student> students = studentRepository.findStudentsBySchoolIdAndActiveSession(schoolId);

        return students.stream()
                .map(student -> buildCatalogForStudent(student, schoolId))
                .collect(Collectors.toList());
    }

    @Override
    public FeeCatalogDto getFeeCatalogByStudentPanNumber(String panNumber, Long schoolId)
    {
        Student student = studentRepository.findByPanNumberIgnoreCaseAndSchool_Id(panNumber, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: " + panNumber));

        return buildCatalogForStudent(student, schoolId);
    }

    private FeeCatalogDto buildCatalogForStudent(Student student, Long schoolId)
    {
        // Use student's own session, not the active session
        // This ensures we show fees for the session the student is enrolled in
        Session studentSession = student.getSession();
        if (studentSession == null) {
            throw new ResourceNotFoundException("Student " + student.getPanNumber() + 
                    " is not enrolled in any session");
        }
        
        List<Fee> fees = feeRepository.findByStudentPanNumberAndSchoolIdOrderByYearAscMonthAsc(
                student.getPanNumber(), schoolId, studentSession.getId());

        log.info("Found {} fees for student {} in session {}", 
                fees.size(), student.getPanNumber(), studentSession.getName());

        LocalDate today = LocalDate.now();
        
        // Check and update overdue fees on-the-fly
        boolean hasUpdates = false;
        for (Fee fee : fees)
        {
            if (fee.getStatus() == FeeStatus.PENDING && fee.getDueDate() != null && fee.getDueDate().isBefore(today))
            {
                fee.setStatus(FeeStatus.OVERDUE);
                hasUpdates = true;
            }
        }
        
        // Save updated fees if any were marked as overdue
        if (hasUpdates)
        {
            feeRepository.saveAll(fees);
        }

        Map<FeeMonth, Fee> feeMap = fees.stream()
                .collect(Collectors.toMap(Fee::getMonth, Function.identity(), (existing, replacement) -> existing));

        FeeCatalogDto catalog = new FeeCatalogDto();
        catalog.setStudentId(student.getPanNumber());

        List<MonthlyFeeDto> monthlyFees = new ArrayList<>();
        double totalAmount = 0;
        double totalPaid = 0;
        double totalPending = 0;
        double totalOverdue = 0;

        FeeMonth sessionStartMonth = studentSession.getStartMonth();

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
                    case UNPAID -> totalPending += fee.getAmount();
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
    public void markPendingFeesAsOverdue(Long schoolId)
    {
        LocalDate today = LocalDate.now();
        List<Fee> overdueFees = feeRepository
                .findOverdueFeesByStudentAndActiveSession(
                        FeeStatus.PENDING, today, schoolId);

        overdueFees.forEach(fee -> fee.setStatus(FeeStatus.OVERDUE));
        feeRepository.saveAll(overdueFees);

        log.info("Marked {} pending fees as OVERDUE as of {}", overdueFees.size(), today);
    }

    @Override
    @Transactional
    public void generateFeesForStudent(String panNumber)
    {
        Student student = studentRepository.findById(panNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: " + panNumber));

        ClassEntity currentClass = student.getCurrentClass();
        if (currentClass == null)
        {
            throw new ResourceNotFoundException("Student with PAN " + panNumber + " is not enrolled in any class");
        }

        Session session = student.getSession();
        if (session == null)
        {
            throw new ResourceNotFoundException("Student with PAN " + panNumber + " is not enrolled in any session");
        }

        // Check if fees already exist for this student in this session
        List<Fee> existingFees = feeRepository.findByStudentPanNumberAndSchoolIdOrderByYearAscMonthAsc(
                panNumber, student.getSchool().getId(), session.getId());
        
        if (!existingFees.isEmpty())
        {
            log.warn("Fees already exist for student {} in session {}. Found {} existing fees.", 
                    panNumber, session.getName(), existingFees.size());
            throw new AlreadyExistException("Fee records already exist for student " + panNumber + 
                    " in session " + session.getName() + ". Total existing fees: " + existingFees.size());
        }

        // Use repository to find fee structure instead of relying on OneToOne relationship
        FeeStructure feeStructure = feeStructureRepository
                .findByClassEntity_IdAndSession_IdAndSchool_Id(
                        currentClass.getId(), 
                        session.getId(), 
                        student.getSchool().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No fee structure found for class: " + currentClass.getClassName() + 
                        " in session: " + session.getName() + " for school ID: " + student.getSchool().getId()));

        log.info("Generating 12-month fees for student {} in class {} for session {}", 
                panNumber, currentClass.getClassName(), session.getName());

        // Generate fees for 12 consecutive months starting from session start date
        LocalDate currentMonth = session.getStartDate().withDayOfMonth(1);
        List<Fee> feesToCreate = new ArrayList<>();

        for (int i = 0; i < 12; i++)
        {
            FeeMonth feeMonth = FeeMonth.valueOf(currentMonth.getMonth().name());
            int year = currentMonth.getYear();

            Fee fee = new Fee();
            fee.setStudent(student);
            fee.setMonth(feeMonth);
            fee.setYear(year);
            fee.setFeeStructure(feeStructure);
            fee.setClassEntity(currentClass);
            fee.setAmount(feeStructure.getFeesAmount());
            fee.setStatus(FeeStatus.PENDING);
            fee.setDueDate(LocalDate.of(year, currentMonth.getMonth(), 10)); // Due on 10th of each month
            fee.setSchool(student.getSchool());
            fee.setSession(session); // Link fee to session

            feesToCreate.add(fee);
            
            // Move to next month
            currentMonth = currentMonth.plusMonths(1);
        }

        // Save all fees at once
        feeRepository.saveAll(feesToCreate);
        log.info("Successfully generated {} fee records for student {} in session {}", 
                feesToCreate.size(), panNumber, session.getName());
    }

    private LocalDate calculateDueDate(FeeMonth month, Session session)
    {
        // Calculate due date as 10th of each month
        int monthNumber = month.ordinal() + 4; // APRIL = 0, so +4 gives 4 (April)
        if (monthNumber > 12)
        {
            monthNumber -= 12; // Wrap around to next year
        }
        
        int year = session.getStartDate().getYear();
        if (monthNumber < session.getStartDate().getMonthValue())
        {
            year++; // If month is before session start month, it's next year
        }

        return LocalDate.of(year, monthNumber, 10);
    }
}
