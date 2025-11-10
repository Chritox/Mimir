package de.chritox.mimir.services;

import de.chritox.mimir.models.Training;
import de.chritox.mimir.repositories.TrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainingService {
    private final TrainingRepository trainingRepository;

    public List<Training> findAll() {
        return trainingRepository.findAll();
    }

    public Optional<Training> findById(Long id) {
        return trainingRepository.findById(id);
    }

    @Transactional
    public Training save(Training training) {
        return trainingRepository.save(training);
    }

    @Transactional
    public void deleteById(Long id) {
        trainingRepository.deleteById(id);
    }
}
