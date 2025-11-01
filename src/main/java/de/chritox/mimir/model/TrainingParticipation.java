package de.chritox.mimir.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "training_participations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    /**
     * Date when the employee completed the training
     */
    @Column(nullable = false)
    private LocalDate completionDate;

    /**
     * Date when the next training is due (calculated based on completion date and interval)
     */
    private LocalDate nextDueDate;

    /**
     * Optional notes or comments about the training participation
     */
    @Column(length = 1000)
    private String notes;
}
