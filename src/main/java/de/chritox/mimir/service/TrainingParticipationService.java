package de.chritox.mimir.service;

import de.chritox.mimir.model.Employee;
import de.chritox.mimir.model.Training;
import de.chritox.mimir.model.TrainingParticipation;
import de.chritox.mimir.repository.TrainingParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingParticipationService {

    private final TrainingParticipationRepository participationRepository;

    public List<TrainingParticipation> getAllParticipations() {
        return participationRepository.findAll();
    }

    public Optional<TrainingParticipation> getParticipationById(Long id) {
        return participationRepository.findById(id);
    }

    public List<TrainingParticipation> getParticipationsByEmployee(Employee employee) {
        return participationRepository.findByEmployee(employee);
    }

    public List<TrainingParticipation> getParticipationsByTraining(Training training) {
        return participationRepository.findByTraining(training);
    }

    public List<TrainingParticipation> getOverdueParticipations() {
        return participationRepository.findByNextDueDateBefore(LocalDate.now());
    }

    @Transactional
    public TrainingParticipation saveParticipation(TrainingParticipation participation) {
        // Calculate next due date based on completion date and training interval
        if (participation.getCompletionDate() != null && 
            participation.getTraining() != null && 
            participation.getTraining().getIntervalMonths() != null) {
            LocalDate nextDueDate = participation.getCompletionDate()
                .plusMonths(participation.getTraining().getIntervalMonths());
            participation.setNextDueDate(nextDueDate);
        }
        return participationRepository.save(participation);
    }

    @Transactional
    public void deleteParticipation(Long id) {
        participationRepository.deleteById(id);
    }
}
