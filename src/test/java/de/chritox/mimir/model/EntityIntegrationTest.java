package de.chritox.mimir.model;

import de.chritox.mimir.repository.EmployeeRepository;
import de.chritox.mimir.repository.TrainingParticipationRepository;
import de.chritox.mimir.repository.TrainingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EntityIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingParticipationRepository participationRepository;

    @Test
    public void testEmployeeCreation() {
        // Given
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmployeeNumber("EMP001");
        employee.setEmail("john.doe@example.com");
        employee.setDepartment("IT");

        // When
        Employee saved = employeeRepository.save(employee);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
    }

    @Test
    public void testTrainingCreation() {
        // Given
        Training training = new Training();
        training.setName("Fire Safety Training");
        training.setDescription("Annual fire safety training");
        training.setIntervalMonths(12);
        training.setMandatory(true);

        // When
        Training saved = trainingRepository.save(training);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Fire Safety Training");
        assertThat(saved.getIntervalMonths()).isEqualTo(12);
        assertThat(saved.getMandatory()).isTrue();
    }

    @Test
    public void testTrainingParticipation() {
        // Given
        Employee employee = new Employee();
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        employee.setEmployeeNumber("EMP002");
        employee.setEmail("jane.smith@example.com");
        employee = employeeRepository.save(employee);

        Training training = new Training();
        training.setName("Data Protection Training");
        training.setDescription("GDPR compliance training");
        training.setIntervalMonths(24);
        training.setMandatory(true);
        training = trainingRepository.save(training);

        TrainingParticipation participation = new TrainingParticipation();
        participation.setEmployee(employee);
        participation.setTraining(training);
        participation.setCompletionDate(LocalDate.now());
        participation.setNextDueDate(LocalDate.now().plusMonths(24));
        participation.setNotes("Completed successfully");

        // When
        TrainingParticipation saved = participationRepository.save(participation);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmployee().getId()).isEqualTo(employee.getId());
        assertThat(saved.getTraining().getId()).isEqualTo(training.getId());
        assertThat(saved.getCompletionDate()).isEqualTo(LocalDate.now());
    }

    @Test
    public void testFindParticipationsByEmployee() {
        // Given
        Employee employee = new Employee();
        employee.setFirstName("Bob");
        employee.setLastName("Wilson");
        employee.setEmployeeNumber("EMP003");
        employee.setEmail("bob.wilson@example.com");
        employee = employeeRepository.save(employee);

        Training training1 = new Training();
        training1.setName("Training 1");
        training1.setIntervalMonths(12);
        training1 = trainingRepository.save(training1);

        Training training2 = new Training();
        training2.setName("Training 2");
        training2.setIntervalMonths(12);
        training2 = trainingRepository.save(training2);

        TrainingParticipation participation1 = new TrainingParticipation();
        participation1.setEmployee(employee);
        participation1.setTraining(training1);
        participation1.setCompletionDate(LocalDate.now());
        participationRepository.save(participation1);

        TrainingParticipation participation2 = new TrainingParticipation();
        participation2.setEmployee(employee);
        participation2.setTraining(training2);
        participation2.setCompletionDate(LocalDate.now());
        participationRepository.save(participation2);

        // When
        List<TrainingParticipation> participations = participationRepository.findByEmployee(employee);

        // Then
        assertThat(participations).hasSize(2);
    }

    @Test
    public void testFindMandatoryTrainings() {
        // Given
        Training mandatory = new Training();
        mandatory.setName("Mandatory Training");
        mandatory.setIntervalMonths(12);
        mandatory.setMandatory(true);
        trainingRepository.save(mandatory);

        Training optional = new Training();
        optional.setName("Optional Training");
        optional.setIntervalMonths(12);
        optional.setMandatory(false);
        trainingRepository.save(optional);

        // When
        List<Training> mandatoryTrainings = trainingRepository.findByMandatory(true);

        // Then
        assertThat(mandatoryTrainings).hasSize(1);
        assertThat(mandatoryTrainings.get(0).getName()).isEqualTo("Mandatory Training");
    }
}
