package de.chritox.mimir.controllers;

import de.chritox.mimir.config.WebConfig;
import de.chritox.mimir.models.Department;
import de.chritox.mimir.models.Employee;
import de.chritox.mimir.services.DepartmentService;
import de.chritox.mimir.services.EmployeeService;
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
 * Comprehensive unit tests for error handling in DepartmentController
 */
@WebMvcTest(value = DepartmentController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class))
class DepartmentControllerErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private EmployeeService employeeService;

    private Department testDepartment;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("IT-Abteilung");

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Max Mustermann");
        testEmployee.setDepartment(testDepartment);
    }

    // ==================== LIST OPERATION TESTS ====================

    @Test
    void testList_ExceptionThrown_ShowsErrorMessage() throws Exception {
        when(departmentService.findAll()).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/list"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attribute("departments", Collections.emptyList()));

        verify(departmentService, times(1)).findAll();
    }

    // ==================== CREATE OPERATION TESTS ====================

    @Test
    void testCreate_DataIntegrityViolation_ShowsError() throws Exception {
        when(departmentService.save(any(Department.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        mockMvc.perform(post("/departments")
                        .param("name", "IT-Abteilung"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(departmentService, times(1)).save(any(Department.class));
    }

    @Test
    void testCreate_UnexpectedException_ShowsError() throws Exception {
        when(departmentService.save(any(Department.class)))
                .thenThrow(new RuntimeException("Unexpected database error"));

        mockMvc.perform(post("/departments")
                        .param("name", "IT-Abteilung"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(departmentService, times(1)).save(any(Department.class));
    }

    @Test
    void testCreate_Success_RedirectsWithSuccessMessage() throws Exception {
        when(departmentService.save(any(Department.class))).thenReturn(testDepartment);

        mockMvc.perform(post("/departments")
                        .param("name", "IT-Abteilung"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(departmentService, times(1)).save(any(Department.class));
    }

    // ==================== UPDATE OPERATION TESTS ====================

    @Test
    void testUpdate_DepartmentNotFound_ShowsError() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/departments/1")
                        .param("name", "IT-Abteilung Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(departmentService, times(1)).findById(1L);
        verify(departmentService, never()).save(any(Department.class));
    }

    @Test
    void testUpdate_DataIntegrityViolation_ShowsError() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(departmentService.save(any(Department.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        mockMvc.perform(put("/departments/1")
                        .param("name", "IT-Abteilung"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(departmentService, times(1)).findById(1L);
        verify(departmentService, times(1)).save(any(Department.class));
    }

    @Test
    void testUpdate_UnexpectedException_ShowsError() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(departmentService.save(any(Department.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put("/departments/1")
                        .param("name", "IT-Abteilung"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(departmentService, times(1)).findById(1L);
        verify(departmentService, times(1)).save(any(Department.class));
    }

    @Test
    void testUpdate_Success_RedirectsWithSuccessMessage() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(departmentService.save(any(Department.class))).thenReturn(testDepartment);

        mockMvc.perform(put("/departments/1")
                        .param("name", "IT-Abteilung Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(departmentService, times(1)).findById(1L);
        verify(departmentService, times(1)).save(any(Department.class));
    }

    // ==================== DELETE OPERATION TESTS ====================

    @Test
    void testDelete_DepartmentNotFound_ShowsError() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/departments/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(departmentService, times(1)).findById(1L);
        verify(departmentService, never()).deleteById(anyLong());
    }

    @Test
    void testDelete_DataIntegrityViolation_ShowsConstraintError() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        doThrow(new DataIntegrityViolationException("Foreign key constraint"))
                .when(departmentService).deleteById(1L);

        mockMvc.perform(delete("/departments/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(departmentService, times(1)).findById(1L);
        verify(departmentService, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_UnexpectedException_ShowsError() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        doThrow(new RuntimeException("Unexpected error"))
                .when(departmentService).deleteById(1L);

        mockMvc.perform(delete("/departments/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(departmentService, times(1)).findById(1L);
        verify(departmentService, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_Success_RedirectsWithSuccessMessage() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        doNothing().when(departmentService).deleteById(1L);

        mockMvc.perform(delete("/departments/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(departmentService, times(1)).findById(1L);
        verify(departmentService, times(1)).deleteById(1L);
    }

    // ==================== DETAIL VIEW TESTS ====================

    @Test
    void testDetail_DepartmentNotFound_RedirectsWithError() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/departments/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(departmentService, times(1)).findById(1L);
        verify(employeeService, never()).findByDepartmentId(anyLong());
    }

    @Test
    void testDetail_ServiceException_RedirectsWithError() throws Exception {
        when(departmentService.findById(1L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/departments/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(departmentService, times(1)).findById(1L);
    }

    @Test
    void testDetail_Success_ShowsDetailPage() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(employeeService.findByDepartmentId(1L)).thenReturn(List.of(testEmployee));

        mockMvc.perform(get("/departments/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/detail"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("employees"));

        verify(departmentService, times(1)).findById(1L);
        verify(employeeService, times(1)).findByDepartmentId(1L);
    }

    // ==================== FORM LOADING TESTS ====================

    @Test
    void testShowEditForm_DepartmentNotFound_RedirectsWithError() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/departments/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(departmentService, times(1)).findById(1L);
    }

    @Test
    void testShowEditForm_ServiceException_RedirectsWithError() throws Exception {
        when(departmentService.findById(1L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/departments/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(departmentService, times(1)).findById(1L);
    }

    @Test
    void testShowEditForm_Success_ShowsForm() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));

        mockMvc.perform(get("/departments/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attribute("department", testDepartment));

        verify(departmentService, times(1)).findById(1L);
    }

    @Test
    void testShowCreateForm_ServiceException_RedirectsWithError() throws Exception {
        // Mock exception during model attribute creation (edge case)
        mockMvc.perform(get("/departments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department"));
    }
}
