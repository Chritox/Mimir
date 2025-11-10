package de.chritox.mimir.controllers;

import de.chritox.mimir.config.WebConfig;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for error handling in EmployeeController
 */
@WebMvcTest(value = EmployeeController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class))
class EmployeeControllerErrorHandlingTest {

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

        testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTitle("First Aid");

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Max Mustermann");
        testEmployee.setDepartment(testDepartment);
    }

    // ==================== LIST OPERATION TESTS ====================

    @Test
    void testList_ExceptionThrown_ShowsErrorMessage() throws Exception {
        when(employeeService.findAll()).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attribute("employees", Collections.emptyList()));

        verify(employeeService, times(1)).findAll();
    }

    // ==================== CREATE OPERATION TESTS ====================

    @Test
    void testCreate_DataIntegrityViolation_ShowsError() throws Exception {
        when(employeeService.save(any(Employee.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));
        when(departmentService.findAll()).thenReturn(List.of(testDepartment));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));

        mockMvc.perform(post("/employees")
                        .param("name", "Max Mustermann"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("trainings"));

        verify(employeeService, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreate_GenericException_ShowsError() throws Exception {
        when(employeeService.save(any(Employee.class)))
                .thenThrow(new RuntimeException("Unexpected error"));
        when(departmentService.findAll()).thenReturn(List.of(testDepartment));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));

        mockMvc.perform(post("/employees")
                        .param("name", "Max Mustermann"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(employeeService, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreate_Success_ShowsSuccessMessage() throws Exception {
        when(employeeService.save(any(Employee.class))).thenReturn(testEmployee);

        mockMvc.perform(post("/employees")
                        .param("name", "Max Mustermann"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(employeeService, times(1)).save(any(Employee.class));
    }

    // ==================== UPDATE OPERATION TESTS ====================

    @Test
    void testUpdate_EmployeeNotFound_RedirectsWithError() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/employees/1")
                        .param("name", "Max Mustermann"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(employeeService, times(1)).findById(1L);
        verify(employeeService, never()).save(any(Employee.class));
    }

    @Test
    void testUpdate_DataIntegrityViolation_ShowsError() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeService.save(any(Employee.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));
        when(departmentService.findAll()).thenReturn(List.of(testDepartment));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));

        mockMvc.perform(put("/employees/1")
                        .param("name", "Max Mustermann"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(employeeService, times(1)).save(any(Employee.class));
    }

    @Test
    void testUpdate_Success_ShowsSuccessMessage() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeService.save(any(Employee.class))).thenReturn(testEmployee);

        mockMvc.perform(put("/employees/1")
                        .param("name", "Max Mustermann Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(employeeService, times(1)).save(any(Employee.class));
    }

    // ==================== DELETE OPERATION TESTS ====================

    @Test
    void testDelete_EmployeeNotFound_RedirectsWithError() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(employeeService, times(1)).findById(1L);
        verify(employeeService, never()).deleteById(anyLong());
    }

    @Test
    void testDelete_DataIntegrityViolation_ShowsConstraintError() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.of(testEmployee));
        doThrow(new DataIntegrityViolationException("Foreign key constraint"))
                .when(employeeService).deleteById(1L);

        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(employeeService, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_GenericException_ShowsError() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.of(testEmployee));
        doThrow(new RuntimeException("Unexpected error"))
                .when(employeeService).deleteById(1L);

        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(employeeService, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_Success_ShowsSuccessMessageWithName() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.of(testEmployee));
        doNothing().when(employeeService).deleteById(1L);

        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(employeeService, times(1)).deleteById(1L);
    }

    // ==================== DETAIL VIEW TESTS ====================

    @Test
    void testDetail_EmployeeNotFound_RedirectsWithError() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(employeeService, times(1)).findById(1L);
    }

    @Test
    void testDetail_GenericException_RedirectsWithError() throws Exception {
        when(employeeService.findById(1L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(employeeService, times(1)).findById(1L);
    }

    // ==================== EDIT FORM TESTS ====================

    @Test
    void testShowEditForm_EmployeeNotFound_RedirectsWithError() throws Exception {
        when(employeeService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/employees/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(employeeService, times(1)).findById(1L);
        verify(departmentService, never()).findAll();
        verify(trainingService, never()).findAll();
    }

    @Test
    void testShowEditForm_GenericException_RedirectsWithError() throws Exception {
        when(employeeService.findById(1L)).thenThrow(new RuntimeException("Service unavailable"));

        mockMvc.perform(get("/employees/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(employeeService, times(1)).findById(1L);
    }

    // ==================== CREATE FORM TESTS ====================

    @Test
    void testShowCreateForm_ExceptionLoadingDependencies_RedirectsWithError() throws Exception {
        when(departmentService.findAll()).thenThrow(new RuntimeException("Cannot load departments"));

        mockMvc.perform(get("/employees/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(departmentService, times(1)).findAll();
    }
}
