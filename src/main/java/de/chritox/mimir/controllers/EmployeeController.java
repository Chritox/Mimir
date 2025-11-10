package de.chritox.mimir.controllers;

import de.chritox.mimir.models.Employee;
import de.chritox.mimir.services.DepartmentService;
import de.chritox.mimir.services.EmployeeService;
import de.chritox.mimir.services.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final TrainingService trainingService;

    @GetMapping
    public String list(Model model) {
        try {
            model.addAttribute("employees", employeeService.findAll());
            return "employees/list";
        } catch (Exception e) {
            log.error("Error loading employee list", e);
            model.addAttribute("errorMessage", "Fehler beim Laden der Mitarbeiterliste: " + e.getMessage());
            model.addAttribute("employees", java.util.Collections.emptyList());
            return "employees/list";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        try {
            model.addAttribute("employee", new Employee());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("trainings", trainingService.findAll());
            return "employees/form";
        } catch (Exception e) {
            log.error("Error loading employee create form", e);
            model.addAttribute("errorMessage", "Fehler beim Laden des Formulars: " + e.getMessage());
            return "redirect:/employees";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Mitarbeiter mit ID " + id + " nicht gefunden"));
            model.addAttribute("employee", employee);
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("trainings", trainingService.findAll());
            return "employees/form";
        } catch (IllegalArgumentException e) {
            log.warn("Employee not found: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employees";
        } catch (Exception e) {
            log.error("Error loading employee edit form for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Laden des Mitarbeiters: " + e.getMessage());
            return "redirect:/employees";
        }
    }

    @PostMapping
    public String create(@ModelAttribute Employee employee, BindingResult result, 
                        Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors creating employee: {}", result.getAllErrors());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("trainings", trainingService.findAll());
            return "employees/form";
        }
        
        try {
            employeeService.save(employee);
            log.info("Created employee: {}", employee.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Mitarbeiter erfolgreich erstellt: " + employee.getName());
            return "redirect:/employees";
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation creating employee", e);
            model.addAttribute("errorMessage", "Fehler beim Speichern: Möglicherweise existiert bereits ein Mitarbeiter mit diesen Daten");
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("trainings", trainingService.findAll());
            return "employees/form";
        } catch (Exception e) {
            log.error("Error creating employee", e);
            model.addAttribute("errorMessage", "Fehler beim Erstellen des Mitarbeiters: " + e.getMessage());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("trainings", trainingService.findAll());
            return "employees/form";
        }
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Employee employee, 
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors updating employee {}: {}", id, result.getAllErrors());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("trainings", trainingService.findAll());
            return "employees/form";
        }
        
        try {
            if (!employeeService.findById(id).isPresent()) {
                throw new IllegalArgumentException("Mitarbeiter mit ID " + id + " nicht gefunden");
            }
            employee.setId(id);
            employeeService.save(employee);
            log.info("Updated employee: {}", employee.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Mitarbeiter erfolgreich aktualisiert: " + employee.getName());
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            log.warn("Employee not found for update: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employees";
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation updating employee {}", id, e);
            model.addAttribute("errorMessage", "Fehler beim Speichern: Datenkonflikt");
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("trainings", trainingService.findAll());
            return "employees/form";
        } catch (Exception e) {
            log.error("Error updating employee {}", id, e);
            model.addAttribute("errorMessage", "Fehler beim Aktualisieren des Mitarbeiters: " + e.getMessage());
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("trainings", trainingService.findAll());
            return "employees/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Mitarbeiter mit ID " + id + " nicht gefunden"));
            String employeeName = employee.getName();
            employeeService.deleteById(id);
            log.info("Deleted employee: {} (ID: {})", employeeName, id);
            redirectAttributes.addFlashAttribute("successMessage", "Mitarbeiter erfolgreich gelöscht: " + employeeName);
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            log.warn("Employee not found for deletion: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employees";
        } catch (DataIntegrityViolationException e) {
            log.error("Cannot delete employee {} due to data integrity constraint", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Mitarbeiter kann nicht gelöscht werden: Er ist noch mit Schulungsterminen verknüpft");
            return "redirect:/employees";
        } catch (Exception e) {
            log.error("Error deleting employee {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Löschen des Mitarbeiters: " + e.getMessage());
            return "redirect:/employees";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Mitarbeiter mit ID " + id + " nicht gefunden"));
            model.addAttribute("employee", employee);
            return "employees/detail";
        } catch (IllegalArgumentException e) {
            log.warn("Employee not found: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employees";
        } catch (Exception e) {
            log.error("Error loading employee detail for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Laden der Mitarbeiterdetails: " + e.getMessage());
            return "redirect:/employees";
        }
    }
}
