package de.chritox.mimir.services;

import de.chritox.mimir.models.Department;
import de.chritox.mimir.models.Employee;
import de.chritox.mimir.models.Training;
import de.chritox.mimir.models.TrainingSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final TrainingSessionService trainingSessionService;

    public Map<Training, LocalDate> getDueTrainingsForEmployee(Employee employee, LocalDate targetDate) {
        Map<Training, LocalDate> dueTrainings = new HashMap<>();
        
        if (employee.getMandatoryTrainings() == null || employee.getMandatoryTrainings().isEmpty()) {
            return dueTrainings;
        }
        
        for (Training training : employee.getMandatoryTrainings()) {
            LocalDate lastAttended = getLastAttendedDate(employee, training);
            
            if (lastAttended == null) {
                // Never attended - due immediately
                dueTrainings.put(training, targetDate);
            } else if (training.getInterval() != null) {
                // Calculate next due date
                LocalDate nextDueDate = lastAttended.plusMonths(training.getInterval());
                if (!nextDueDate.isAfter(targetDate)) {
                    dueTrainings.put(training, nextDueDate);
                }
            }
        }
        
        return dueTrainings;
    }
    
    private LocalDate getLastAttendedDate(Employee employee, Training training) {
        if (employee.getAttendedSessions() == null) {
            return null;
        }
        
        return employee.getAttendedSessions().stream()
                .filter(session -> session.getTraining() != null && 
                                 session.getTraining().getId().equals(training.getId()))
                .map(TrainingSession::getDate)
                .max(LocalDate::compareTo)
                .orElse(null);
    }
    
    public byte[] generateDepartmentTrainingReport(LocalDate targetDate) throws IOException {
        List<Department> departments = departmentService.findAll();
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            for (Department department : departments) {
                List<Employee> employees = employeeService.findByDepartmentId(department.getId());
                createDepartmentSheet(workbook, department, employees, targetDate, headerStyle, dateStyle);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private void createDepartmentSheet(Workbook workbook, Department department, 
                                      List<Employee> employees, LocalDate targetDate,
                                      CellStyle headerStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet(sanitizeSheetName(department.getName()));
        
        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Mitarbeiter", "Schulung", "Letzte Teilnahme", "Fällig am", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        int rowNum = 1;
        for (Employee employee : employees) {
            Map<Training, LocalDate> dueTrainings = getDueTrainingsForEmployee(employee, targetDate);
            
            if (dueTrainings.isEmpty()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(employee.getName());
                row.createCell(1).setCellValue("Keine fälligen Schulungen");
                row.createCell(4).setCellValue("Aktuell");
            } else {
                for (Map.Entry<Training, LocalDate> entry : dueTrainings.entrySet()) {
                    Training training = entry.getKey();
                    LocalDate dueDate = entry.getValue();
                    LocalDate lastAttended = getLastAttendedDate(employee, training);
                    
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(employee.getName());
                    row.createCell(1).setCellValue(training.getTitle());
                    
                    if (lastAttended != null) {
                        Cell dateCell = row.createCell(2);
                        dateCell.setCellValue(lastAttended.toString());
                        dateCell.setCellStyle(dateStyle);
                    } else {
                        row.createCell(2).setCellValue("Noch nie");
                    }
                    
                    Cell dueDateCell = row.createCell(3);
                    dueDateCell.setCellValue(dueDate.toString());
                    dueDateCell.setCellStyle(dateStyle);
                    
                    row.createCell(4).setCellValue(dueDate.isBefore(LocalDate.now()) ? "Überfällig" : "Fällig");
                }
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd.mm.yyyy"));
        return style;
    }
    
    private String sanitizeSheetName(String name) {
        if (name == null) return "Sheet";
        // Excel sheet names cannot contain: \ / ? * [ ]
        // and must be max 31 characters
        String sanitized = name.replaceAll("[\\\\/?*\\[\\]]", "_");
        return sanitized.length() > 31 ? sanitized.substring(0, 31) : sanitized;
    }
}
