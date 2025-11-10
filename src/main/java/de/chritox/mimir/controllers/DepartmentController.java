package de.chritox.mimir.controllers;

import de.chritox.mimir.models.Department;
import de.chritox.mimir.services.DepartmentService;
import de.chritox.mimir.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    @GetMapping
    public String list(Model model) {
        try {
            model.addAttribute("departments", departmentService.findAll());
            return "departments/list";
        } catch (Exception e) {
            log.error("Error loading department list", e);
            model.addAttribute("errorMessage", "Fehler beim Laden der Abteilungsliste: " + e.getMessage());
            model.addAttribute("departments", java.util.Collections.emptyList());
            return "departments/list";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        try {
            model.addAttribute("department", new Department());
            return "departments/form";
        } catch (Exception e) {
            log.error("Error loading department create form", e);
            model.addAttribute("errorMessage", "Fehler beim Laden des Formulars: " + e.getMessage());
            return "redirect:/departments";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Abteilung mit ID " + id + " nicht gefunden"));
            model.addAttribute("department", department);
            return "departments/form";
        } catch (IllegalArgumentException e) {
            log.warn("Department not found: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/departments";
        } catch (Exception e) {
            log.error("Error loading department edit form for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Laden der Abteilung: " + e.getMessage());
            return "redirect:/departments";
        }
    }

    @PostMapping
    public String create(@ModelAttribute Department department, BindingResult result,
                        Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors creating department: {}", result.getAllErrors());
            return "departments/form";
        }
        
        try {
            departmentService.save(department);
            log.info("Created department: {}", department.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Abteilung erfolgreich erstellt: " + department.getName());
            return "redirect:/departments";
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation creating department", e);
            model.addAttribute("errorMessage", "Fehler beim Speichern: Möglicherweise existiert bereits eine Abteilung mit diesem Namen");
            return "departments/form";
        } catch (Exception e) {
            log.error("Error creating department", e);
            model.addAttribute("errorMessage", "Fehler beim Erstellen der Abteilung: " + e.getMessage());
            return "departments/form";
        }
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Department department,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors updating department {}: {}", id, result.getAllErrors());
            return "departments/form";
        }
        
        try {
            if (!departmentService.findById(id).isPresent()) {
                throw new IllegalArgumentException("Abteilung mit ID " + id + " nicht gefunden");
            }
            department.setId(id);
            departmentService.save(department);
            log.info("Updated department: {}", department.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Abteilung erfolgreich aktualisiert: " + department.getName());
            return "redirect:/departments";
        } catch (IllegalArgumentException e) {
            log.warn("Department not found for update: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/departments";
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation updating department {}", id, e);
            model.addAttribute("errorMessage", "Fehler beim Speichern: Datenkonflikt");
            return "departments/form";
        } catch (Exception e) {
            log.error("Error updating department {}", id, e);
            model.addAttribute("errorMessage", "Fehler beim Aktualisieren der Abteilung: " + e.getMessage());
            return "departments/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Abteilung mit ID " + id + " nicht gefunden"));
            String departmentName = department.getName();
            departmentService.deleteById(id);
            log.info("Deleted department: {} (ID: {})", departmentName, id);
            redirectAttributes.addFlashAttribute("successMessage", "Abteilung erfolgreich gelöscht: " + departmentName);
            return "redirect:/departments";
        } catch (IllegalArgumentException e) {
            log.warn("Department not found for deletion: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/departments";
        } catch (DataIntegrityViolationException e) {
            log.error("Cannot delete department {} due to data integrity constraint", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Abteilung kann nicht gelöscht werden: Sie enthält noch Mitarbeiter");
            return "redirect:/departments";
        } catch (Exception e) {
            log.error("Error deleting department {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Löschen der Abteilung: " + e.getMessage());
            return "redirect:/departments";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Abteilung mit ID " + id + " nicht gefunden"));
            model.addAttribute("department", department);
            model.addAttribute("employees", employeeService.findByDepartmentId(id));
            return "departments/detail";
        } catch (IllegalArgumentException e) {
            log.warn("Department not found: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/departments";
        } catch (Exception e) {
            log.error("Error loading department detail for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Laden der Abteilungsdetails: " + e.getMessage());
            return "redirect:/departments";
        }
    }
}
