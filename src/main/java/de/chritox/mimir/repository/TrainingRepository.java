package de.chritox.mimir.repository;

import de.chritox.mimir.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    
    List<Training> findByMandatory(Boolean mandatory);
}
