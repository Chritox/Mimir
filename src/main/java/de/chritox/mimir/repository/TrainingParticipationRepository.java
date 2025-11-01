package de.chritox.mimir.repository;

import de.chritox.mimir.model.Employee;
import de.chritox.mimir.model.Training;
import de.chritox.mimir.model.TrainingParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingParticipationRepository extends JpaRepository<TrainingParticipation, Long> {
    
    List<TrainingParticipation> findByEmployee(Employee employee);
    
    List<TrainingParticipation> findByTraining(Training training);
    
    List<TrainingParticipation> findByNextDueDateBefore(LocalDate date);
    
    List<TrainingParticipation> findByEmployeeAndTraining(Employee employee, Training training);
}
