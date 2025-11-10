package de.chritox.mimir.controllers;

import de.chritox.mimir.models.Training;
import de.chritox.mimir.services.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/trainings")
@RequiredArgsConstructor
public class TrainingController {
    private final TrainingService trainingService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("trainings", trainingService.findAll());
        return "trainings/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("training", new Training());
        return "trainings/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Training training = trainingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid training Id: " + id));
        model.addAttribute("training", training);
        return "trainings/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Training training) {
        trainingService.save(training);
        return "redirect:/trainings";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        trainingService.deleteById(id);
        return "redirect:/trainings";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Training training = trainingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid training Id: " + id));
        model.addAttribute("training", training);
        return "trainings/detail";
    }
}
