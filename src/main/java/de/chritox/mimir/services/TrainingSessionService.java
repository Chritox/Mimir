package de.chritox.mimir.services;

import de.chritox.mimir.models.TrainingSession;
import de.chritox.mimir.repositories.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainingSessionService {
    private final TrainingSessionRepository trainingSessionRepository;

    public List<TrainingSession> findAll() {
        return trainingSessionRepository.findAll();
    }

    public Optional<TrainingSession> findById(Long id) {
        return trainingSessionRepository.findById(id);
    }

    public List<TrainingSession> findByTrainingId(Long trainingId) {
        return trainingSessionRepository.findByTrainingId(trainingId);
    }

    public List<TrainingSession> findUpcoming() {
        return trainingSessionRepository.findByDateAfter(LocalDate.now());
    }

    @Transactional
    public TrainingSession save(TrainingSession session) {
        return trainingSessionRepository.save(session);
    }

    @Transactional
    public void deleteById(Long id) {
        trainingSessionRepository.deleteById(id);
    }
}
