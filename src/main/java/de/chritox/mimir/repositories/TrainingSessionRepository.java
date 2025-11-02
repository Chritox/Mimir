package de.chritox.mimir.repositories;

import de.chritox.mimir.models.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    List<TrainingSession> findByTrainingId(Long trainingId);
    List<TrainingSession> findByDateAfter(LocalDate date);
}
