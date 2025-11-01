package de.chritox.mimir.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    /**
     * Training interval in months (e.g., 12 for yearly, 24 for every two years)
     */
    @Column(nullable = false)
    private Integer intervalMonths;

    /**
     * Indicates if this training is mandatory for employees
     */
    private Boolean mandatory = false;

    @OneToMany(mappedBy = "training", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TrainingParticipation> trainingParticipations = new HashSet<>();
}
