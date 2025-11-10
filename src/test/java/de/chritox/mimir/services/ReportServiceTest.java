package de.chritox.mimir.services;

import de.chritox.mimir.models.Department;
import de.chritox.mimir.models.Employee;
import de.chritox.mimir.models.Training;
import de.chritox.mimir.models.TrainingSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DepartmentService departmentService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private TrainingSessionService trainingSessionService;

    @InjectMocks
    private ReportService reportService;

    private Training testTraining;
    private Employee testEmployee;
    private Department testDepartment;
    private TrainingSession testSession;

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("IT");
        testDepartment.setDescription("IT Department");

        testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTitle("Erste Hilfe");
        testTraining.setDescription("Grundlagen");
        testTraining.setInterval(24); // 24 months

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Max Mustermann");
        testEmployee.setDepartment(testDepartment);
        testEmployee.setMandatoryTrainings(new HashSet<>(Set.of(testTraining)));
        testEmployee.setAttendedSessions(new HashSet<>());

        testSession = new TrainingSession();
        testSession.setId(1L);
        testSession.setTraining(testTraining);
        testSession.setDate(LocalDate.now().minusMonths(25));
        testSession.setParticipants(new HashSet<>());
    }

    @Test
    void testGetDueTrainingsForEmployee_NeverAttended() {
        LocalDate targetDate = LocalDate.now();
        
        Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(testEmployee, targetDate);
        
        assertEquals(1, dueTrainings.size());
        assertTrue(dueTrainings.containsKey(testTraining));
        assertEquals(targetDate, dueTrainings.get(testTraining));
    }

    @Test
    void testGetDueTrainingsForEmployee_AttendedButOverdue() {
        LocalDate targetDate = LocalDate.now();
        testEmployee.getAttendedSessions().add(testSession);
        
        Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(testEmployee, targetDate);
        
        assertEquals(1, dueTrainings.size());
        assertTrue(dueTrainings.containsKey(testTraining));
        LocalDate expectedDueDate = testSession.getDate().plusMonths(testTraining.getInterval());
        assertEquals(expectedDueDate, dueTrainings.get(testTraining));
    }

    @Test
    void testGetDueTrainingsForEmployee_AttendedRecently() {
        LocalDate targetDate = LocalDate.now();
        testSession.setDate(LocalDate.now().minusMonths(6)); // Attended 6 months ago
        testEmployee.getAttendedSessions().add(testSession);
        
        Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(testEmployee, targetDate);
        
        assertEquals(0, dueTrainings.size());
    }

    @Test
    void testGetDueTrainingsForEmployee_NoMandatoryTrainings() {
        testEmployee.setMandatoryTrainings(new HashSet<>());
        LocalDate targetDate = LocalDate.now();
        
        Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(testEmployee, targetDate);
        
        assertEquals(0, dueTrainings.size());
    }

    @Test
    void testGetDueTrainingsForEmployee_NullMandatoryTrainings() {
        testEmployee.setMandatoryTrainings(null);
        LocalDate targetDate = LocalDate.now();
        
        Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(testEmployee, targetDate);
        
        assertEquals(0, dueTrainings.size());
    }

    @Test
    void testGetDueTrainingsForEmployee_TrainingWithoutInterval() {
        testTraining.setInterval(null);
        testSession.setDate(LocalDate.now().minusMonths(25));
        testEmployee.getAttendedSessions().add(testSession);
        LocalDate targetDate = LocalDate.now();
        
        Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(testEmployee, targetDate);
        
        // Training without interval is not due if already attended
        assertEquals(0, dueTrainings.size());
    }

    @Test
    void testGetDueTrainingsForEmployee_MultipleTrainings() {
        Training training2 = new Training();
        training2.setId(2L);
        training2.setTitle("Brandschutz");
        training2.setInterval(12);
        
        testEmployee.getMandatoryTrainings().add(training2);
        LocalDate targetDate = LocalDate.now();
        
        Map<Training, LocalDate> dueTrainings = reportService.getDueTrainingsForEmployee(testEmployee, targetDate);
        
        assertEquals(2, dueTrainings.size());
        assertTrue(dueTrainings.containsKey(testTraining));
        assertTrue(dueTrainings.containsKey(training2));
    }

    @Test
    void testGenerateDepartmentTrainingReport() throws IOException {
        when(departmentService.findAll()).thenReturn(List.of(testDepartment));
        when(employeeService.findByDepartmentId(1L)).thenReturn(List.of(testEmployee));
        
        LocalDate targetDate = LocalDate.now();
        byte[] excelData = reportService.generateDepartmentTrainingReport(targetDate);
        
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        verify(departmentService, times(1)).findAll();
        verify(employeeService, times(1)).findByDepartmentId(1L);
    }

    @Test
    void testGenerateDepartmentTrainingReport_MultipleDepartments() throws IOException {
        Department dept2 = new Department();
        dept2.setId(2L);
        dept2.setName("HR");
        
        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setName("Anna Schmidt");
        employee2.setDepartment(dept2);
        employee2.setMandatoryTrainings(new HashSet<>());
        employee2.setAttendedSessions(new HashSet<>());
        
        when(departmentService.findAll()).thenReturn(List.of(testDepartment, dept2));
        when(employeeService.findByDepartmentId(1L)).thenReturn(List.of(testEmployee));
        when(employeeService.findByDepartmentId(2L)).thenReturn(List.of(employee2));
        
        LocalDate targetDate = LocalDate.now();
        byte[] excelData = reportService.generateDepartmentTrainingReport(targetDate);
        
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        verify(departmentService, times(1)).findAll();
        verify(employeeService, times(1)).findByDepartmentId(1L);
        verify(employeeService, times(1)).findByDepartmentId(2L);
    }

    @Test
    void testGenerateDepartmentTrainingReport_EmptyDepartments() throws IOException {
        when(departmentService.findAll()).thenReturn(Collections.emptyList());
        
        LocalDate targetDate = LocalDate.now();
        byte[] excelData = reportService.generateDepartmentTrainingReport(targetDate);
        
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        verify(departmentService, times(1)).findAll();
    }
}
