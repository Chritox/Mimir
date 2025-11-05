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
                // Never attended - mark as null to display "Überfällig"
                dueTrainings.put(training, null);
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
    
    public LocalDate getLastAttendedDate(Employee employee, Training training) {
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
        
        // Title and summary section
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);
        CellStyle overdueStyle = createOverdueStyle(workbook);
        CellStyle dueStyle = createDueStyle(workbook);
        CellStyle currentStyle = createCurrentStyle(workbook);
        
        int rowNum = 0;
        
        // Department title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Schulungsbedarfsanalyse - " + department.getName());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));
        
        // Report date
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Stichtag:");
        Cell reportDateCell = dateRow.createCell(1);
        reportDateCell.setCellValue(targetDate);
        reportDateCell.setCellStyle(dateStyle);
        
        // Employee count
        Row countRow = sheet.createRow(rowNum++);
        countRow.createCell(0).setCellValue("Anzahl Mitarbeiter:");
        countRow.createCell(1).setCellValue(employees.size());
        
        // Empty row
        rowNum++;
        
        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Mitarbeiter", "Schulung", "Letzte Teilnahme", "Fällig am / Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        for (Employee employee : employees) {
            Map<Training, LocalDate> dueTrainings = getDueTrainingsForEmployee(employee, targetDate);
            
            if (dueTrainings.isEmpty()) {
                Row row = sheet.createRow(rowNum++);
                Cell nameCell = row.createCell(0);
                nameCell.setCellValue(employee.getName());
                nameCell.setCellStyle(normalStyle);
                
                Cell trainingCell = row.createCell(1);
                trainingCell.setCellValue("Keine fälligen Schulungen");
                trainingCell.setCellStyle(normalStyle);
                
                row.createCell(2).setCellStyle(normalStyle);
                
                Cell dueDateCell = row.createCell(3);
                dueDateCell.setCellValue("Aktuell");
                dueDateCell.setCellStyle(currentStyle);
            } else {
                for (Map.Entry<Training, LocalDate> entry : dueTrainings.entrySet()) {
                    Training training = entry.getKey();
                    LocalDate dueDate = entry.getValue();
                    LocalDate lastAttended = getLastAttendedDate(employee, training);
                    
                    Row row = sheet.createRow(rowNum++);
                    
                    Cell nameCell = row.createCell(0);
                    nameCell.setCellValue(employee.getName());
                    nameCell.setCellStyle(normalStyle);
                    
                    Cell trainingCell = row.createCell(1);
                    trainingCell.setCellValue(training.getTitle());
                    trainingCell.setCellStyle(normalStyle);
                    
                    Cell lastAttendedCell = row.createCell(2);
                    if (lastAttended != null) {
                        lastAttendedCell.setCellValue(lastAttended);
                        lastAttendedCell.setCellStyle(dateStyle);
                    } else {
                        lastAttendedCell.setCellValue("Noch nie");
                        lastAttendedCell.setCellStyle(normalStyle);
                    }
                    
                    Cell dueDateCell = row.createCell(3);
                    if (dueDate == null) {
                        // Never attended - show as overdue
                        dueDateCell.setCellValue("Überfällig");
                        dueDateCell.setCellStyle(overdueStyle);
                    } else {
                        boolean isOverdue = dueDate.isBefore(LocalDate.now());
                        if (isOverdue) {
                            dueDateCell.setCellValue("Überfällig seit " + dueDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                            dueDateCell.setCellStyle(overdueStyle);
                        } else {
                            dueDateCell.setCellValue(dueDate);
                            dueDateCell.setCellStyle(dateStyle);
                        }
                    }
                }
            }
        }
        
        // Set column widths
        sheet.setColumnWidth(0, 6000);  // Name
        sheet.setColumnWidth(1, 8000);  // Training
        sheet.setColumnWidth(2, 4000);  // Last attended
        sheet.setColumnWidth(3, 5000);  // Due date/Status
        
        // Freeze panes (freeze header row)
        sheet.createFreezePane(0, 5);
    }
    
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);
        return style;
    }
    
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd.mm.yyyy"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }
    
    private CellStyle createOverdueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createDueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createCurrentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
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
