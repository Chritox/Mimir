package de.chritox.mimir.service;

import de.chritox.mimir.model.Training;
import de.chritox.mimir.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingService {

    private final TrainingRepository trainingRepository;

    public List<Training> getAllTrainings() {
        return trainingRepository.findAll();
    }

    public Optional<Training> getTrainingById(Long id) {
        return trainingRepository.findById(id);
    }

    public List<Training> getMandatoryTrainings() {
        return trainingRepository.findByMandatory(true);
    }

    @Transactional
    public Training saveTraining(Training training) {
        return trainingRepository.save(training);
    }

    @Transactional
    public void deleteTraining(Long id) {
        trainingRepository.deleteById(id);
    }
}
