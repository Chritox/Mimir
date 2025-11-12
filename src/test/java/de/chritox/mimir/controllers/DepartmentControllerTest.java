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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for DepartmentController
 */
@WebMvcTest(value = DepartmentController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class))
class DepartmentControllerTest {

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

    @Test
    void testListDepartments() throws Exception {
        when(departmentService.findAll()).thenReturn(List.of(testDepartment));

        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/list"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attribute("departments", List.of(testDepartment)));

        verify(departmentService, times(1)).findAll();
    }

    @Test
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/departments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department"));
    }

    @Test
    void testShowEditForm() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));

        mockMvc.perform(get("/departments/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/form"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attribute("department", testDepartment));

        verify(departmentService, times(1)).findById(1L);
    }

    @Test
    void testCreateDepartment() throws Exception {
        when(departmentService.save(any(Department.class))).thenReturn(testDepartment);

        mockMvc.perform(post("/departments")
                        .param("name", "IT-Abteilung"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(departmentService, times(1)).save(any(Department.class));
    }

    @Test
    void testUpdateDepartment() throws Exception {
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

    @Test
    void testDeleteDepartment() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        doNothing().when(departmentService).deleteById(1L);

        mockMvc.perform(delete("/departments/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/departments"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(departmentService, times(1)).findById(1L);
        verify(departmentService, times(1)).deleteById(1L);
    }

    @Test
    void testDetailView() throws Exception {
        when(departmentService.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(employeeService.findByDepartmentId(1L)).thenReturn(List.of(testEmployee));

        mockMvc.perform(get("/departments/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("departments/detail"))
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attribute("department", testDepartment));

        verify(departmentService, times(1)).findById(1L);
        verify(employeeService, times(1)).findByDepartmentId(1L);
    }
}
