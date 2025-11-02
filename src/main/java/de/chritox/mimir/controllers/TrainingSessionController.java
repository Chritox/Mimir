package de.chritox.mimir.controllers;

import de.chritox.mimir.models.TrainingSession;
import de.chritox.mimir.services.EmployeeService;
import de.chritox.mimir.services.TrainingService;
import de.chritox.mimir.services.TrainingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class TrainingSessionController {
    private final TrainingSessionService sessionService;
    private final TrainingService trainingService;
    private final EmployeeService employeeService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("sessions", sessionService.findAll());
        return "sessions/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("session", new TrainingSession());
        model.addAttribute("trainings", trainingService.findAll());
        model.addAttribute("employees", employeeService.findAll());
        return "sessions/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        TrainingSession session = sessionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session Id: " + id));
        model.addAttribute("session", session);
        model.addAttribute("trainings", trainingService.findAll());
        model.addAttribute("employees", employeeService.findAll());
        return "sessions/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute TrainingSession session) {
        sessionService.save(session);
        return "redirect:/sessions";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        sessionService.deleteById(id);
        return "redirect:/sessions";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        TrainingSession session = sessionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session Id: " + id));
        model.addAttribute("session", session);
        return "sessions/detail";
    }

    @GetMapping("/upcoming")
    public String upcoming(Model model) {
        model.addAttribute("sessions", sessionService.findUpcoming());
        return "sessions/upcoming";
    }
}
