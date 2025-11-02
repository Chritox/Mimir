package de.chritox.mimir.controllers;

import de.chritox.mimir.models.Employee;
import de.chritox.mimir.services.DepartmentService;
import de.chritox.mimir.services.EmployeeService;
import de.chritox.mimir.services.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final TrainingService trainingService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "employees/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("trainings", trainingService.findAll());
        return "employees/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));
        model.addAttribute("employee", employee);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("trainings", trainingService.findAll());
        return "employees/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Employee employee) {
        employeeService.save(employee);
        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        employeeService.deleteById(id);
        return "redirect:/employees";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));
        model.addAttribute("employee", employee);
        return "employees/detail";
    }
}
