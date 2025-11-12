package de.chritox.mimir.controllers;

import de.chritox.mimir.models.Training;
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
@RequestMapping("/trainings")
@RequiredArgsConstructor
@Slf4j
public class TrainingController {
    private final TrainingService trainingService;

    @GetMapping
    public String list(Model model) {
        try {
            model.addAttribute("trainings", trainingService.findAll());
            return "trainings/list";
        } catch (Exception e) {
            log.error("Error loading training list", e);
            model.addAttribute("errorMessage", "Fehler beim Laden der Schulungsliste: " + e.getMessage());
            model.addAttribute("trainings", java.util.Collections.emptyList());
            return "trainings/list";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        try {
            model.addAttribute("training", new Training());
            return "trainings/form";
        } catch (Exception e) {
            log.error("Error loading training create form", e);
            model.addAttribute("errorMessage", "Fehler beim Laden des Formulars: " + e.getMessage());
            return "redirect:/trainings";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Training training = trainingService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Schulung mit ID " + id + " nicht gefunden"));
            model.addAttribute("training", training);
            return "trainings/form";
        } catch (IllegalArgumentException e) {
            log.warn("Training not found: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/trainings";
        } catch (Exception e) {
            log.error("Error loading training edit form for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Laden der Schulung: " + e.getMessage());
            return "redirect:/trainings";
        }
    }

    @PostMapping
    public String create(@ModelAttribute Training training, BindingResult result,
                        Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors creating training: {}", result.getAllErrors());
            return "trainings/form";
        }
        
        try {
            trainingService.save(training);
            log.info("Created training: {}", training.getTitle());
            redirectAttributes.addFlashAttribute("successMessage", "Schulung erfolgreich erstellt: " + training.getTitle());
            return "redirect:/trainings";
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation creating training", e);
            model.addAttribute("errorMessage", "Fehler beim Speichern: Möglicherweise existiert bereits eine Schulung mit diesem Titel");
            return "trainings/form";
        } catch (Exception e) {
            log.error("Error creating training", e);
            model.addAttribute("errorMessage", "Fehler beim Erstellen der Schulung: " + e.getMessage());
            return "trainings/form";
        }
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Training training,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors updating training {}: {}", id, result.getAllErrors());
            return "trainings/form";
        }
        
        try {
            if (!trainingService.findById(id).isPresent()) {
                throw new IllegalArgumentException("Schulung mit ID " + id + " nicht gefunden");
            }
            training.setId(id);
            trainingService.save(training);
            log.info("Updated training: {}", training.getTitle());
            redirectAttributes.addFlashAttribute("successMessage", "Schulung erfolgreich aktualisiert: " + training.getTitle());
            return "redirect:/trainings";
        } catch (IllegalArgumentException e) {
            log.warn("Training not found for update: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/trainings";
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation updating training {}", id, e);
            model.addAttribute("errorMessage", "Fehler beim Speichern: Datenkonflikt");
            return "trainings/form";
        } catch (Exception e) {
            log.error("Error updating training {}", id, e);
            model.addAttribute("errorMessage", "Fehler beim Aktualisieren der Schulung: " + e.getMessage());
            return "trainings/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Training training = trainingService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Schulung mit ID " + id + " nicht gefunden"));
            String trainingTitle = training.getTitle();
            trainingService.deleteById(id);
            log.info("Deleted training: {} (ID: {})", trainingTitle, id);
            redirectAttributes.addFlashAttribute("successMessage", "Schulung erfolgreich gelöscht: " + trainingTitle);
            return "redirect:/trainings";
        } catch (IllegalArgumentException e) {
            log.warn("Training not found for deletion: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/trainings";
        } catch (DataIntegrityViolationException e) {
            log.error("Cannot delete training {} due to data integrity constraint", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Schulung kann nicht gelöscht werden: Sie ist noch mit Mitarbeitern oder Terminen verknüpft");
            return "redirect:/trainings";
        } catch (Exception e) {
            log.error("Error deleting training {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Löschen der Schulung: " + e.getMessage());
            return "redirect:/trainings";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Training training = trainingService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Schulung mit ID " + id + " nicht gefunden"));
            model.addAttribute("training", training);
            return "trainings/detail";
        } catch (IllegalArgumentException e) {
            log.warn("Training not found: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/trainings";
        } catch (Exception e) {
            log.error("Error loading training detail for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Laden der Schulungsdetails: " + e.getMessage());
            return "redirect:/trainings";
        }
    }
}
