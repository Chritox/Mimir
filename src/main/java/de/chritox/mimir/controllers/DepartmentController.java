package de.chritox.mimir.controllers;

import de.chritox.mimir.models.Department;
import de.chritox.mimir.services.DepartmentService;
import de.chritox.mimir.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("departments", departmentService.findAll());
        return "departments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new Department());
        return "departments/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Department department = departmentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid department Id:" + id));
        model.addAttribute("department", department);
        return "departments/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Department department) {
        departmentService.save(department);
        return "redirect:/departments";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        departmentService.deleteById(id);
        return "redirect:/departments";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Department department = departmentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid department Id:" + id));
        model.addAttribute("department", department);
        model.addAttribute("employees", employeeService.findByDepartmentId(id));
        return "departments/detail";
    }
}
