package de.chritox.mimir.controllers;

import de.chritox.mimir.models.Employee;
import de.chritox.mimir.models.Training;
import de.chritox.mimir.services.DepartmentService;
import de.chritox.mimir.services.EmployeeService;
import de.chritox.mimir.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final ReportService reportService;

    @GetMapping("/training-needs")
    public String trainingNeeds(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String targetDate,
            Model model) {
        
        LocalDate date = targetDate != null && !targetDate.isEmpty() 
            ? LocalDate.parse(targetDate) 
            : LocalDate.now();
        
        var allDepartments = departmentService.findAll();
        model.addAttribute("departments", allDepartments);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("targetDate", date);
        
        if (departmentId != null) {
            // Single department view
            List<Employee> employees = employeeService.findByDepartmentId(departmentId);
            
            // Calculate due trainings for each employee
            Map<Long, Map<Training, LocalDate>> employeeDueTrainings = new HashMap<>();
            Map<Long, Map<Training, LocalDate>> employeeLastAttended = new HashMap<>();
            for (Employee employee : employees) {
                Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(employee, date);
                employeeDueTrainings.put(employee.getId(), dueTrainings);
                
                // Also get last attended dates
                Map<Training, LocalDate> lastAttended = new HashMap<>();
                for (Training training : dueTrainings.keySet()) {
                    lastAttended.put(training, reportService.getLastAttendedDate(employee, training));
                }
                employeeLastAttended.put(employee.getId(), lastAttended);
            }
            
            model.addAttribute("employees", employees);
            model.addAttribute("employeeDueTrainings", employeeDueTrainings);
            model.addAttribute("employeeLastAttended", employeeLastAttended);
            model.addAttribute("selectedDepartment", 
                departmentService.findById(departmentId).orElse(null));
        } else {
            // Show all departments
            Map<Long, List<Employee>> departmentEmployees = new HashMap<>();
            Map<Long, Map<Long, Map<Training, LocalDate>>> allDueTrainings = new HashMap<>();
            Map<Long, Map<Long, Map<Training, LocalDate>>> allLastAttended = new HashMap<>();
            
            for (var department : allDepartments) {
                List<Employee> employees = employeeService.findByDepartmentId(department.getId());
                departmentEmployees.put(department.getId(), employees);
                
                Map<Long, Map<Training, LocalDate>> employeeDueTrainings = new HashMap<>();
                Map<Long, Map<Training, LocalDate>> employeeLastAttended = new HashMap<>();
                for (Employee employee : employees) {
                    Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(employee, date);
                    employeeDueTrainings.put(employee.getId(), dueTrainings);
                    
                    // Also get last attended dates
                    Map<Training, LocalDate> lastAttended = new HashMap<>();
                    for (Training training : dueTrainings.keySet()) {
                        lastAttended.put(training, reportService.getLastAttendedDate(employee, training));
                    }
                    employeeLastAttended.put(employee.getId(), lastAttended);
                }
                allDueTrainings.put(department.getId(), employeeDueTrainings);
                allLastAttended.put(department.getId(), employeeLastAttended);
            }
            
            model.addAttribute("departmentEmployees", departmentEmployees);
            model.addAttribute("allDueTrainings", allDueTrainings);
            model.addAttribute("allLastAttended", allLastAttended);
        }
        
        return "reports/training-needs";
    }
    
    @GetMapping("/training-needs/export")
    public ResponseEntity<byte[]> exportTrainingNeeds(
            @RequestParam(required = false) String targetDate) throws IOException {
        
        LocalDate date = targetDate != null && !targetDate.isEmpty() 
            ? LocalDate.parse(targetDate) 
            : LocalDate.now();
        
        byte[] excelData = reportService.generateDepartmentTrainingReport(date);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", 
            "schulungsbedarf_" + date.toString() + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
    
    @GetMapping("/training-needs/print")
    public String printAllDepartments(
            @RequestParam(required = false) String targetDate,
            Model model) {
        
        LocalDate date = targetDate != null && !targetDate.isEmpty() 
            ? LocalDate.parse(targetDate) 
            : LocalDate.now();
        
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("targetDate", date);
        
        // Calculate due trainings for all departments
        Map<Long, List<Employee>> departmentEmployees = new HashMap<>();
        Map<Long, Map<Long, Map<Training, LocalDate>>> allDueTrainings = new HashMap<>();
        Map<Long, Map<Long, Map<Training, LocalDate>>> allLastAttended = new HashMap<>();
        
        for (var department : departmentService.findAll()) {
            List<Employee> employees = employeeService.findByDepartmentId(department.getId());
            departmentEmployees.put(department.getId(), employees);
            
            Map<Long, Map<Training, LocalDate>> employeeDueTrainings = new HashMap<>();
            Map<Long, Map<Training, LocalDate>> employeeLastAttended = new HashMap<>();
            for (Employee employee : employees) {
                Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(employee, date);
                employeeDueTrainings.put(employee.getId(), dueTrainings);
                
                // Also get last attended dates
                Map<Training, LocalDate> lastAttended = new HashMap<>();
                for (Training training : dueTrainings.keySet()) {
                    lastAttended.put(training, reportService.getLastAttendedDate(employee, training));
                }
                employeeLastAttended.put(employee.getId(), lastAttended);
            }
            allDueTrainings.put(department.getId(), employeeDueTrainings);
            allLastAttended.put(department.getId(), employeeLastAttended);
        }
        
        model.addAttribute("departmentEmployees", departmentEmployees);
        model.addAttribute("allDueTrainings", allDueTrainings);
        model.addAttribute("allLastAttended", allLastAttended);
        
        return "reports/print-all";
    }
    
    @GetMapping("/training-needs/print-department")
    public String printDepartment(
            @RequestParam Long departmentId,
            @RequestParam(required = false) String targetDate,
            Model model) {
        
        LocalDate date = targetDate != null && !targetDate.isEmpty() 
            ? LocalDate.parse(targetDate) 
            : LocalDate.now();
        
        var department = departmentService.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid department Id: " + departmentId));
        List<Employee> employees = employeeService.findByDepartmentId(departmentId);
        
        // Calculate due trainings for each employee and count statistics
        Map<Long, Map<Training, LocalDate>> employeeDueTrainings = new HashMap<>();
        Map<Long, Map<Training, LocalDate>> employeeLastAttended = new HashMap<>();
        int employeesWithNeeds = 0;
        int overdueCount = 0;
        
        for (Employee employee : employees) {
            Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(employee, date);
            employeeDueTrainings.put(employee.getId(), dueTrainings);
            
            // Also get last attended dates
            Map<Training, LocalDate> lastAttended = new HashMap<>();
            for (Training training : dueTrainings.keySet()) {
                lastAttended.put(training, reportService.getLastAttendedDate(employee, training));
            }
            employeeLastAttended.put(employee.getId(), lastAttended);
            
            if (!dueTrainings.isEmpty()) {
                employeesWithNeeds++;
                for (LocalDate dueDate : dueTrainings.values()) {
                    // null date means never attended = overdue
                    if (dueDate == null || dueDate.isBefore(LocalDate.now())) {
                        overdueCount++;
                    }
                }
            }
        }
        
        model.addAttribute("department", department);
        model.addAttribute("employees", employees);
        model.addAttribute("employeeDueTrainings", employeeDueTrainings);
        model.addAttribute("employeeLastAttended", employeeLastAttended);
        model.addAttribute("targetDate", date);
        model.addAttribute("employeesWithNeeds", employeesWithNeeds);
        model.addAttribute("overdueCount", overdueCount);
        
        return "reports/print-department";
    }
    
    @GetMapping("/employee-training-report")
    public String employeeTrainingReport(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String targetDate,
            Model model) {
        
        LocalDate date = targetDate != null && !targetDate.isEmpty() 
            ? LocalDate.parse(targetDate) 
            : LocalDate.now();
        
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("targetDate", date);
        
        if (employeeId != null) {
            Employee employee = employeeService.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id: " + employeeId));
            
            Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(employee, date);
            
            model.addAttribute("employee", employee);
            model.addAttribute("dueTrainings", dueTrainings);
        }
        
        return "reports/employee-training-report";
    }
    
    @GetMapping("/employee-training-report/print")
    public String printEmployeeReport(
            @RequestParam Long employeeId,
            @RequestParam(required = false) String targetDate,
            Model model) {
        
        LocalDate date = targetDate != null && !targetDate.isEmpty() 
            ? LocalDate.parse(targetDate) 
            : LocalDate.now();
        
        Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id: " + employeeId));
        
        Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(employee, date);
        
        // Count overdue trainings (null date means never attended = overdue)
        int overdueCount = (int) dueTrainings.values().stream()
                .filter(dueDate -> dueDate == null || dueDate.isBefore(LocalDate.now()))
                .count();
        
        model.addAttribute("employee", employee);
        model.addAttribute("dueTrainings", dueTrainings);
        model.addAttribute("targetDate", date);
        model.addAttribute("overdueCount", overdueCount);
        
        return "reports/print-employee";
    }
}
