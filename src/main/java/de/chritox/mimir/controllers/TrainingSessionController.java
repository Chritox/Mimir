package de.chritox.mimir.controllers;

import de.chritox.mimir.models.TrainingSession;
import de.chritox.mimir.services.EmployeeService;
import de.chritox.mimir.services.TrainingService;
import de.chritox.mimir.services.TrainingSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Slf4j
public class TrainingSessionController {
    private final TrainingSessionService sessionService;
    private final TrainingService trainingService;
    private final EmployeeService employeeService;

    @GetMapping
    public String list(Model model) {
        try {
            model.addAttribute("sessions", sessionService.findAll());
            return "sessions/list";
        } catch (Exception e) {
            log.error("Error loading session list", e);
            model.addAttribute("errorMessage", "Fehler beim Laden der Terminliste: " + e.getMessage());
            model.addAttribute("sessions", java.util.Collections.emptyList());
            return "sessions/list";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        try {
            model.addAttribute("trainingSession", new TrainingSession());
            model.addAttribute("trainings", trainingService.findAll());
            model.addAttribute("employees", employeeService.findAll());
            return "sessions/form";
        } catch (Exception e) {
            log.error("Error loading session create form", e);
            model.addAttribute("errorMessage", "Fehler beim Laden des Formulars: " + e.getMessage());
            return "redirect:/sessions";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            TrainingSession trainingSession = sessionService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Schulungstermin mit ID " + id + " nicht gefunden"));
            model.addAttribute("trainingSession", trainingSession);
            model.addAttribute("trainings", trainingService.findAll());
            model.addAttribute("employees", employeeService.findAll());
            return "sessions/form";
        } catch (IllegalArgumentException e) {
            log.warn("Session not found: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sessions";
        } catch (Exception e) {
            log.error("Error loading session edit form for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Laden des Schulungstermins: " + e.getMessage());
            return "redirect:/sessions";
        }
    }

    @PostMapping
    public String create(@ModelAttribute("trainingSession") TrainingSession trainingSession,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors creating session: {}", result.getAllErrors());
            model.addAttribute("trainings", trainingService.findAll());
            model.addAttribute("employees", employeeService.findAll());
            return "sessions/form";
        }
        
        try {
            sessionService.save(trainingSession);
            log.info("Created session for training: {} on {}", 
                    trainingSession.getTraining() != null ? trainingSession.getTraining().getTitle() : "null",
                    trainingSession.getDate());
            redirectAttributes.addFlashAttribute("successMessage", "Schulungstermin erfolgreich erstellt");
            return "redirect:/sessions";
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation creating session", e);
            model.addAttribute("errorMessage", "Fehler beim Speichern: Datenkonflikt");
            model.addAttribute("trainings", trainingService.findAll());
            model.addAttribute("employees", employeeService.findAll());
            return "sessions/form";
        } catch (Exception e) {
            log.error("Error creating session", e);
            model.addAttribute("errorMessage", "Fehler beim Erstellen des Schulungstermins: " + e.getMessage());
            model.addAttribute("trainings", trainingService.findAll());
            model.addAttribute("employees", employeeService.findAll());
            return "sessions/form";
        }
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("trainingSession") TrainingSession trainingSession,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors updating session {}: {}", id, result.getAllErrors());
            model.addAttribute("trainings", trainingService.findAll());
            model.addAttribute("employees", employeeService.findAll());
            return "sessions/form";
        }
        
        try {
            if (!sessionService.findById(id).isPresent()) {
                throw new IllegalArgumentException("Schulungstermin mit ID " + id + " nicht gefunden");
            }
            trainingSession.setId(id);
            sessionService.save(trainingSession);
            log.info("Updated session: {} on {}", 
                    trainingSession.getTraining() != null ? trainingSession.getTraining().getTitle() : "null",
                    trainingSession.getDate());
            redirectAttributes.addFlashAttribute("successMessage", "Schulungstermin erfolgreich aktualisiert");
            return "redirect:/sessions";
        } catch (IllegalArgumentException e) {
            log.warn("Session not found for update: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sessions";
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation updating session {}", id, e);
            model.addAttribute("errorMessage", "Fehler beim Speichern: Datenkonflikt");
            model.addAttribute("trainings", trainingService.findAll());
            model.addAttribute("employees", employeeService.findAll());
            return "sessions/form";
        } catch (Exception e) {
            log.error("Error updating session {}", id, e);
            model.addAttribute("errorMessage", "Fehler beim Aktualisieren des Schulungstermins: " + e.getMessage());
            model.addAttribute("trainings", trainingService.findAll());
            model.addAttribute("employees", employeeService.findAll());
            return "sessions/form";
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            TrainingSession trainingSession = sessionService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Schulungstermin mit ID " + id + " nicht gefunden"));
            sessionService.deleteById(id);
            log.info("Deleted session: {} on {} (ID: {})", 
                    trainingSession.getTraining() != null ? trainingSession.getTraining().getTitle() : "null",
                    trainingSession.getDate(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Schulungstermin erfolgreich gelöscht");
            return "redirect:/sessions";
        } catch (IllegalArgumentException e) {
            log.warn("Session not found for deletion: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sessions";
        } catch (Exception e) {
            log.error("Error deleting session {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Löschen des Schulungstermins: " + e.getMessage());
            return "redirect:/sessions";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            TrainingSession trainingSession = sessionService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Schulungstermin mit ID " + id + " nicht gefunden"));
            model.addAttribute("trainingSession", trainingSession);
            return "sessions/detail";
        } catch (IllegalArgumentException e) {
            log.warn("Session not found: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sessions";
        } catch (Exception e) {
            log.error("Error loading session detail for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Laden der Termindetails: " + e.getMessage());
            return "redirect:/sessions";
        }
    }

    @GetMapping("/upcoming")
    public String upcoming(Model model) {
        try {
            model.addAttribute("sessions", sessionService.findUpcoming());
            return "sessions/upcoming";
        } catch (Exception e) {
            log.error("Error loading upcoming sessions", e);
            model.addAttribute("errorMessage", "Fehler beim Laden der kommenden Termine: " + e.getMessage());
            model.addAttribute("sessions", java.util.Collections.emptyList());
            return "sessions/upcoming";
        }
    }
}
