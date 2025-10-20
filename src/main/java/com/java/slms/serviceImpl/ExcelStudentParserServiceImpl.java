package com.java.slms.serviceImpl;

import com.java.slms.dto.StudentRequestDto;
import com.java.slms.repository.SessionRepository;
import com.java.slms.service.ExcelStudentParseService;
import com.java.slms.util.Gender;
import com.java.slms.util.UserStatus;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelStudentParserServiceImpl implements ExcelStudentParseService
{
    private final SessionRepository sessionRepository;

    // Existing Excel upload method
    public List<StudentRequestDto> uploadStudents(MultipartFile file, Long schoolId) throws IOException, CsvValidationException
    {
        if (file.getOriginalFilename().endsWith(".csv"))
        {
            return uploadCsvStudents(file, schoolId);  // If it's a CSV file, parse it using the CSV method
        }
        else
        {
            return uploadExcelStudents(file, schoolId);  // Else parse as Excel
        }
    }

    // Method to parse CSV files
    public List<StudentRequestDto> uploadCsvStudents(MultipartFile file, Long schoolId) throws IOException, CsvValidationException
    {
        List<StudentRequestDto> studentList = new ArrayList<>();

        try (InputStream is = file.getInputStream(); CSVReader csvReader = new CSVReader(new InputStreamReader(is)))
        {
            String[] headers = csvReader.readNext();  // Read headers first
            Map<String, Integer> headerMap = new HashMap<>();

            for (int i = 0; i < headers.length; i++)
            {
                String cleanedHeader = cleanHeader(headers[i]);
                headerMap.put(cleanedHeader, i);
            }

            String[] line;
            while ((line = csvReader.readNext()) != null)
            {
                StudentRequestDto dto = new StudentRequestDto();

                dto.setPanNumber(line[headerMap.get("pan_number")]);
                dto.setAddress(line[headerMap.get("address")]);
                dto.setAdmissionDate(parseLocalDate(line[headerMap.get("admission_date")]));
                dto.setBloodGroup(line[headerMap.get("blood_group")]);
                dto.setClassRollNumber(parseInteger(line[headerMap.get("class_roll_number")]));
                dto.setDateOfBirth(parseLocalDate(line[headerMap.get("date_of_birth")]));
                dto.setEmergencyContact(line[headerMap.get("emergency_contact")]);
                dto.setGender(parseGender(line[headerMap.get("gender")]));
                dto.setMobileNumber(line[headerMap.get("mobile_number")]);
                dto.setName(line[headerMap.get("name")]);
                dto.setParentName(line[headerMap.get("parent_name")]);
                dto.setPhoto(line[headerMap.get("photo")]);
                dto.setPreviousSchool(line[headerMap.get("previous_school")]);
                dto.setStatus(parseStatus(line[headerMap.get("status")]));
                dto.setClassId(parseLong(line[headerMap.get("class_id")]));
                dto.setSessionId(parseLong(line[headerMap.get("session_id")]));
                dto.setUserId(parseLong(line[headerMap.get("user_id")]));

                studentList.add(dto);
            }
        }
        return studentList;
    }

    // Existing method to handle Excel file parsing
    private List<StudentRequestDto> uploadExcelStudents(MultipartFile file, Long schoolId) throws IOException
    {
        List<StudentRequestDto> studentList = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is))
        {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            Map<String, Integer> headerMap = new HashMap<>();

            if (rowIterator.hasNext())
            {
                Row headerRow = rowIterator.next();
                for (Cell cell : headerRow)
                {
                    headerMap.put(cell.getStringCellValue().trim().toLowerCase(), cell.getColumnIndex());
                }
            }

            while (rowIterator.hasNext())
            {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) continue;

                StudentRequestDto dto = new StudentRequestDto();

                dto.setPanNumber(getCellString(row, headerMap, "pan_number"));
                dto.setAddress(getCellString(row, headerMap, "address"));
                dto.setAdmissionDate(parseLocalDate(getCellString(row, headerMap, "admission_date")));
                dto.setBloodGroup(getCellString(row, headerMap, "blood_group"));
                dto.setClassRollNumber(parseInteger(getCellString(row, headerMap, "class_roll_number")));
                dto.setDateOfBirth(parseLocalDate(getCellString(row, headerMap, "date_of_birth")));
                dto.setEmergencyContact(getCellString(row, headerMap, "emergency_contact"));
                dto.setGender(parseGender(getCellString(row, headerMap, "gender")));
                dto.setMobileNumber(getCellString(row, headerMap, "mobile_number"));
                dto.setName(getCellString(row, headerMap, "name"));
                dto.setParentName(getCellString(row, headerMap, "parent_name"));
                dto.setPhoto(getCellString(row, headerMap, "photo"));
                dto.setPreviousSchool(getCellString(row, headerMap, "previous_school"));
                dto.setStatus(parseStatus(getCellString(row, headerMap, "status")));
                dto.setClassId(parseLong(getCellString(row, headerMap, "class_id")));
                dto.setSessionId(parseLong(getCellString(row, headerMap, "session_id")));
                dto.setUserId(parseLong(getCellString(row, headerMap, "user_id")));

                studentList.add(dto);
            }
        }
        return studentList;
    }

    private boolean isRowEmpty(Row row)
    {
        for (Cell cell : row)
        {
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    private String getCellString(Row row, Map<String, Integer> headerMap, String colName)
    {
        Integer idx = headerMap.get(colName.toLowerCase());
        if (idx == null) return null;

        Cell cell = row.getCell(idx);
        if (cell == null) return null;

        switch (cell.getCellType())
        {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                {
                    return new SimpleDateFormat("dd-MM-yyyy").format(cell.getDateCellValue());
                }
                else
                {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return cell.toString().trim();
        }
    }

    private LocalDate parseLocalDate(String dateStr)
    {
        if (StringUtils.hasText(dateStr))
        {
            try
            {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (Exception e)
            {
                return null;
            }
        }
        return null;
    }

    private Date parseDate(String dateStr)
    {
        if (StringUtils.hasText(dateStr))
        {
            try
            {
                return new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(dateStr);
            } catch (Exception e)
            {
                return null;
            }
        }
        return null;
    }

    private Integer parseInteger(String value)
    {
        try
        {
            return value == null ? null : Integer.parseInt(value);
        } catch (NumberFormatException e)
        {
            return null;
        }
    }

    private Long parseLong(String value)
    {
        try
        {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException e)
        {
            return null;
        }
    }

    private Gender parseGender(String value)
    {
        try
        {
            return Gender.valueOf(value.toUpperCase());
        } catch (Exception e)
        {
            return null;
        }
    }

    private UserStatus parseStatus(String value)
    {
        try
        {
            return UserStatus.valueOf(value.toUpperCase());
        } catch (Exception e)
        {
            return UserStatus.ACTIVE;
        }
    }

    private String cleanHeader(String header)
    {
        if (header == null) return null;
        return header.replace("\uFEFF", "")  // removes BOM
                .trim()
                .toLowerCase();
    }

}
