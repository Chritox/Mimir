package de.chritox.mimir.controller;

import de.chritox.mimir.model.Training;
import de.chritox.mimir.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping
    public List<Training> getAllTrainings() {
        return trainingService.getAllTrainings();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Training> getTrainingById(@PathVariable Long id) {
        return trainingService.getTrainingById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mandatory")
    public List<Training> getMandatoryTrainings() {
        return trainingService.getMandatoryTrainings();
    }

    @PostMapping
    public ResponseEntity<Training> createTraining(@RequestBody Training training) {
        Training saved = trainingService.saveTraining(training);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Training> updateTraining(@PathVariable Long id, @RequestBody Training training) {
        return trainingService.getTrainingById(id)
            .map(existing -> {
                training.setId(id);
                Training updated = trainingService.saveTraining(training);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id) {
        if (trainingService.getTrainingById(id).isPresent()) {
            trainingService.deleteTraining(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
