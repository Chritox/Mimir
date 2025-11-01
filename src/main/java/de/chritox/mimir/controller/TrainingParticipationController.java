package de.chritox.mimir.controller;

import de.chritox.mimir.model.TrainingParticipation;
import de.chritox.mimir.service.EmployeeService;
import de.chritox.mimir.service.TrainingParticipationService;
import de.chritox.mimir.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participations")
@RequiredArgsConstructor
public class TrainingParticipationController {

    private final TrainingParticipationService participationService;
    private final EmployeeService employeeService;
    private final TrainingService trainingService;

    @GetMapping
    public List<TrainingParticipation> getAllParticipations() {
        return participationService.getAllParticipations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingParticipation> getParticipationById(@PathVariable Long id) {
        return participationService.getParticipationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TrainingParticipation>> getParticipationsByEmployee(@PathVariable Long employeeId) {
        return employeeService.getEmployeeById(employeeId)
            .map(employee -> ResponseEntity.ok(participationService.getParticipationsByEmployee(employee)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/training/{trainingId}")
    public ResponseEntity<List<TrainingParticipation>> getParticipationsByTraining(@PathVariable Long trainingId) {
        return trainingService.getTrainingById(trainingId)
            .map(training -> ResponseEntity.ok(participationService.getParticipationsByTraining(training)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/overdue")
    public List<TrainingParticipation> getOverdueParticipations() {
        return participationService.getOverdueParticipations();
    }

    @PostMapping
    public ResponseEntity<TrainingParticipation> createParticipation(@RequestBody TrainingParticipation participation) {
        TrainingParticipation saved = participationService.saveParticipation(participation);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingParticipation> updateParticipation(
            @PathVariable Long id, 
            @RequestBody TrainingParticipation participation) {
        return participationService.getParticipationById(id)
            .map(existing -> {
                participation.setId(id);
                TrainingParticipation updated = participationService.saveParticipation(participation);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipation(@PathVariable Long id) {
        if (participationService.getParticipationById(id).isPresent()) {
            participationService.deleteParticipation(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
