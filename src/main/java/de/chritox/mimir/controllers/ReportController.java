package de.chritox.mimir.controllers;

import de.chritox.mimir.services.DepartmentService;
import de.chritox.mimir.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    @GetMapping("/training-needs")
    public String trainingNeeds(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String targetDate,
            Model model) {
        
        LocalDate date = targetDate != null && !targetDate.isEmpty() 
            ? LocalDate.parse(targetDate) 
            : LocalDate.now();
        
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("targetDate", date);
        
        if (departmentId != null) {
            model.addAttribute("employees", employeeService.findByDepartmentId(departmentId));
            model.addAttribute("selectedDepartment", 
                departmentService.findById(departmentId).orElse(null));
        }
        
        return "reports/training-needs";
    }
}
