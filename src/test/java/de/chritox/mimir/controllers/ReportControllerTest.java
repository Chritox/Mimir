package de.chritox.mimir.controllers;

import de.chritox.mimir.models.Department;
import de.chritox.mimir.models.Employee;
import de.chritox.mimir.models.Training;
import de.chritox.mimir.services.DepartmentService;
import de.chritox.mimir.services.EmployeeService;
import de.chritox.mimir.services.ReportService;
import de.chritox.mimir.services.TrainingService;
import de.chritox.mimir.services.TrainingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ReportService reportService;
    
    @MockBean
    private TrainingService trainingService;
    
    @MockBean
    private TrainingSessionService trainingSessionService;

    private Department testDepartment;
    private Employee testEmployee;
    private Training testTraining;

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("IT");
        testDepartment.setDescription("IT Department");

        testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTitle("Erste Hilfe");
        testTraining.setInterval(24);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Max Mustermann");
        testEmployee.setDepartment(testDepartment);
        testEmployee.setMandatoryTrainings(new HashSet<>(Set.of(testTraining)));
        testEmployee.setAttendedSessions(new HashSet<>());
    }

    @Test
    void testTrainingNeeds_NoDepartmentSelected() throws Exception {
        when(departmentService.findAll()).thenReturn(List.of(testDepartment));
        when(employeeService.findByDepartmentId(1L)).thenReturn(List.of(testEmployee));
        when(reportService.getDueTrainingsForEmployee(any(), any())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/reports/training-needs"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/training-needs"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("targetDate"))
                .andExpect(model().attributeExists("departmentEmployees"))
                .andExpect(model().attributeExists("allDueTrainings"))
                .andExpect(model().attribute("selectedDepartmentId", (Object) null));

        verify(departmentService, times(1)).findAll();
        verify(employeeService, times(1)).findByDepartmentId(1L);
    }

    @Test
    void testTrainingNeeds_WithDepartmentSelected() throws Exception {
        when(departmentService.findAll()).thenReturn(List.of(testDepartment));
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(employeeService.findByDepartmentId(1L)).thenReturn(List.of(testEmployee));
        when(reportService.getDueTrainingsForEmployee(any(Employee.class), any(LocalDate.class)))
                .thenReturn(new HashMap<>());

        mockMvc.perform(get("/reports/training-needs")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/training-needs"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("selectedDepartment"))
                .andExpect(model().attributeExists("employeeDueTrainings"))
                .andExpect(model().attribute("selectedDepartmentId", 1L));

        verify(departmentService, times(1)).findAll();
        verify(departmentService, times(1)).findById(1L);
        verify(employeeService, times(1)).findByDepartmentId(1L);
        verify(reportService, times(1)).getDueTrainingsForEmployee(eq(testEmployee), any(LocalDate.class));
    }

    @Test
    void testTrainingNeeds_WithCustomTargetDate() throws Exception {
        when(departmentService.findAll()).thenReturn(List.of(testDepartment));
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(employeeService.findByDepartmentId(1L)).thenReturn(List.of(testEmployee));
        when(reportService.getDueTrainingsForEmployee(any(Employee.class), any(LocalDate.class)))
                .thenReturn(new HashMap<>());

        mockMvc.perform(get("/reports/training-needs")
                        .param("departmentId", "1")
                        .param("targetDate", "2026-01-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/training-needs"))
                .andExpect(model().attribute("targetDate", LocalDate.parse("2026-01-15")));

        verify(reportService, times(1)).getDueTrainingsForEmployee(eq(testEmployee), eq(LocalDate.parse("2026-01-15")));
    }

    @Test
    void testTrainingNeeds_WithMultipleEmployees() throws Exception {
        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setName("Anna Schmidt");
        employee2.setDepartment(testDepartment);
        employee2.setMandatoryTrainings(new HashSet<>());
        employee2.setAttendedSessions(new HashSet<>());

        when(departmentService.findAll()).thenReturn(List.of(testDepartment));
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(employeeService.findByDepartmentId(1L)).thenReturn(List.of(testEmployee, employee2));
        when(reportService.getDueTrainingsForEmployee(any(Employee.class), any(LocalDate.class)))
                .thenReturn(new HashMap<>());

        mockMvc.perform(get("/reports/training-needs")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/training-needs"));

        verify(reportService, times(2)).getDueTrainingsForEmployee(any(Employee.class), any(LocalDate.class));
    }

    @Test
    void testExportTrainingNeeds_DefaultDate() throws Exception {
        byte[] mockExcelData = new byte[]{1, 2, 3, 4, 5};
        when(reportService.generateDepartmentTrainingReport(any(LocalDate.class)))
                .thenReturn(mockExcelData);

        mockMvc.perform(get("/reports/training-needs/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(mockExcelData));

        verify(reportService, times(1)).generateDepartmentTrainingReport(any(LocalDate.class));
    }

    @Test
    void testExportTrainingNeeds_WithCustomDate() throws Exception {
        byte[] mockExcelData = new byte[]{1, 2, 3, 4, 5};
        LocalDate customDate = LocalDate.parse("2026-01-15");
        when(reportService.generateDepartmentTrainingReport(customDate))
                .thenReturn(mockExcelData);

        mockMvc.perform(get("/reports/training-needs/export")
                        .param("targetDate", "2026-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", 
                        "form-data; name=\"attachment\"; filename=\"schulungsbedarf_2026-01-15.xlsx\""))
                .andExpect(content().bytes(mockExcelData));

        verify(reportService, times(1)).generateDepartmentTrainingReport(customDate);
    }

    @Test
    void testExportTrainingNeeds_EmptyReport() throws Exception {
        byte[] emptyExcelData = new byte[0];
        when(reportService.generateDepartmentTrainingReport(any(LocalDate.class)))
                .thenReturn(emptyExcelData);

        mockMvc.perform(get("/reports/training-needs/export"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(emptyExcelData));

        verify(reportService, times(1)).generateDepartmentTrainingReport(any(LocalDate.class));
    }
}
