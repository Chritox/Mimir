package de.chritox.mimir.controllers;

import de.chritox.mimir.models.Department;
import de.chritox.mimir.models.Employee;
import de.chritox.mimir.models.Training;
import de.chritox.mimir.services.DepartmentService;
import de.chritox.mimir.services.EmployeeService;
import de.chritox.mimir.services.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private TrainingService trainingService;

    private Employee testEmployee;
    private Department testDepartment;
    private Training testTraining;

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("IT");
        testDepartment.setDescription("IT Department");

        testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTitle("Test Schulung");
        testTraining.setDescription("Test Beschreibung");
        testTraining.setInterval(12);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Max Mustermann");
        testEmployee.setDepartment(testDepartment);
        testEmployee.setMandatoryTrainings(new HashSet<>());
        testEmployee.setAttendedSessions(new HashSet<>());
    }

    @Test
    void testListEmployees() throws Exception {
        when(employeeService.findAll()).thenReturn(List.of(testEmployee));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(model().attributeExists("employees"));

        verify(employeeService, times(1)).findAll();
    }

    @Test
    void testShowCreateForm() throws Exception {
        when(departmentService.findAll()).thenReturn(List.of(testDepartment));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));

        mockMvc.perform(get("/employees/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("trainings"));

        verify(departmentService, times(1)).findAll();
        verify(trainingService, times(1)).findAll();
    }

    @Test
    void testCreateEmployee() throws Exception {
        when(employeeService.save(any(Employee.class))).thenReturn(testEmployee);

        mockMvc.perform(post("/employees/save")
                        .param("name", "Max Mustermann")
                        .param("department.id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService, times(1)).save(any(Employee.class));
    }

    @Test
    void testShowEditForm() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(departmentService.findAll()).thenReturn(List.of(testDepartment));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));

        mockMvc.perform(get("/employees/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("trainings"));

        verify(employeeService, times(1)).findById(1L);
    }

    @Test
    void testDetailView() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.of(testEmployee));

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/detail"))
                .andExpect(model().attributeExists("employee"));

        verify(employeeService, times(1)).findById(1L);
    }

    @Test
    void testDetailViewWithInvalidId() throws Exception {
        when(employeeService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/employees/999"))
                .andExpect(status().is5xxServerError());

        verify(employeeService, times(1)).findById(999L);
    }

    @Test
    void testDeleteEmployee() throws Exception {
        doNothing().when(employeeService).deleteById(1L);

        mockMvc.perform(get("/employees/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService, times(1)).deleteById(1L);
    }
}
