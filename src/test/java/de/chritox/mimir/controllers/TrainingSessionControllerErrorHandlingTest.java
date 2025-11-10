package de.chritox.mimir.controllers;

import de.chritox.mimir.config.WebConfig;
import de.chritox.mimir.models.Employee;
import de.chritox.mimir.models.Training;
import de.chritox.mimir.models.TrainingSession;
import de.chritox.mimir.services.EmployeeService;
import de.chritox.mimir.services.TrainingService;
import de.chritox.mimir.services.TrainingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for error handling in TrainingSessionController
 */
@WebMvcTest(value = TrainingSessionController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class))
class TrainingSessionControllerErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingSessionService sessionService;

    @MockBean
    private TrainingService trainingService;

    @MockBean
    private EmployeeService employeeService;

    private TrainingSession testSession;
    private Training testTraining;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTitle("First Aid");

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Max Mustermann");

        testSession = new TrainingSession();
        testSession.setId(1L);
        testSession.setTraining(testTraining);
        testSession.setDate(LocalDate.now().plusDays(7));
    }

    // ==================== LIST OPERATION TESTS ====================

    @Test
    void testList_ExceptionThrown_ShowsErrorMessage() throws Exception {
        when(sessionService.findAll()).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/sessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/list"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("sessions"))
                .andExpect(model().attribute("sessions", Collections.emptyList()));

        verify(sessionService, times(1)).findAll();
    }

    // ==================== CREATE OPERATION TESTS ====================

    @Test
    void testCreate_DataIntegrityViolation_ShowsError() throws Exception {
        when(sessionService.save(any(TrainingSession.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));
        when(employeeService.findAll()).thenReturn(List.of(testEmployee));

        mockMvc.perform(post("/sessions")
                        .param("date", "2025-12-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/form"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("trainings"))
                .andExpect(model().attributeExists("employees"));

        verify(sessionService, times(1)).save(any(TrainingSession.class));
    }

    @Test
    void testCreate_UnexpectedException_ShowsError() throws Exception {
        when(sessionService.save(any(TrainingSession.class)))
                .thenThrow(new RuntimeException("Unexpected database error"));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));
        when(employeeService.findAll()).thenReturn(List.of(testEmployee));

        mockMvc.perform(post("/sessions")
                        .param("date", "2025-12-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/form"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("trainings"))
                .andExpect(model().attributeExists("employees"));

        verify(sessionService, times(1)).save(any(TrainingSession.class));
    }

    @Test
    void testCreate_Success_RedirectsWithSuccessMessage() throws Exception {
        when(sessionService.save(any(TrainingSession.class))).thenReturn(testSession);

        mockMvc.perform(post("/sessions")
                        .param("date", "2025-12-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(sessionService, times(1)).save(any(TrainingSession.class));
    }

    // ==================== UPDATE OPERATION TESTS ====================

    @Test
    void testUpdate_SessionNotFound_ShowsError() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/sessions/1")
                        .param("date", "2025-12-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(sessionService, times(1)).findById(1L);
        verify(sessionService, never()).save(any(TrainingSession.class));
    }

    @Test
    void testUpdate_DataIntegrityViolation_ShowsError() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.of(testSession));
        when(sessionService.save(any(TrainingSession.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));
        when(employeeService.findAll()).thenReturn(List.of(testEmployee));

        mockMvc.perform(put("/sessions/1")
                        .param("date", "2025-12-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/form"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("trainings"))
                .andExpect(model().attributeExists("employees"));

        verify(sessionService, times(1)).findById(1L);
        verify(sessionService, times(1)).save(any(TrainingSession.class));
    }

    @Test
    void testUpdate_UnexpectedException_ShowsError() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.of(testSession));
        when(sessionService.save(any(TrainingSession.class)))
                .thenThrow(new RuntimeException("Unexpected error"));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));
        when(employeeService.findAll()).thenReturn(List.of(testEmployee));

        mockMvc.perform(put("/sessions/1")
                        .param("date", "2025-12-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/form"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("trainings"))
                .andExpect(model().attributeExists("employees"));

        verify(sessionService, times(1)).findById(1L);
        verify(sessionService, times(1)).save(any(TrainingSession.class));
    }

    @Test
    void testUpdate_Success_RedirectsWithSuccessMessage() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.of(testSession));
        when(sessionService.save(any(TrainingSession.class))).thenReturn(testSession);

        mockMvc.perform(put("/sessions/1")
                        .param("date", "2025-12-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(sessionService, times(1)).findById(1L);
        verify(sessionService, times(1)).save(any(TrainingSession.class));
    }

    // ==================== DELETE OPERATION TESTS ====================

    @Test
    void testDelete_SessionNotFound_ShowsError() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/sessions/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(sessionService, times(1)).findById(1L);
        verify(sessionService, never()).deleteById(anyLong());
    }

    @Test
    void testDelete_UnexpectedException_ShowsError() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.of(testSession));
        doThrow(new RuntimeException("Unexpected error"))
                .when(sessionService).deleteById(1L);

        mockMvc.perform(delete("/sessions/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(sessionService, times(1)).findById(1L);
        verify(sessionService, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_Success_RedirectsWithSuccessMessage() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.of(testSession));
        doNothing().when(sessionService).deleteById(1L);

        mockMvc.perform(delete("/sessions/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(sessionService, times(1)).findById(1L);
        verify(sessionService, times(1)).deleteById(1L);
    }

    // ==================== DETAIL VIEW TESTS ====================

    @Test
    void testDetail_SessionNotFound_RedirectsWithError() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/sessions/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(sessionService, times(1)).findById(1L);
    }

    @Test
    void testDetail_ServiceException_RedirectsWithError() throws Exception {
        when(sessionService.findById(1L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/sessions/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(sessionService, times(1)).findById(1L);
    }

    @Test
    void testDetail_Success_ShowsDetailPage() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.of(testSession));

        mockMvc.perform(get("/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/detail"))
                .andExpect(model().attributeExists("trainingSession"))
                .andExpect(model().attribute("trainingSession", testSession));

        verify(sessionService, times(1)).findById(1L);
    }

    // ==================== FORM LOADING TESTS ====================

    @Test
    void testShowEditForm_SessionNotFound_RedirectsWithError() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/sessions/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(sessionService, times(1)).findById(1L);
    }

    @Test
    void testShowEditForm_ServiceException_RedirectsWithError() throws Exception {
        when(sessionService.findById(1L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/sessions/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(sessionService, times(1)).findById(1L);
    }

    @Test
    void testShowEditForm_Success_ShowsForm() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.of(testSession));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));
        when(employeeService.findAll()).thenReturn(List.of(testEmployee));

        mockMvc.perform(get("/sessions/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/form"))
                .andExpect(model().attributeExists("trainingSession"))
                .andExpect(model().attributeExists("trainings"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attribute("trainingSession", testSession));

        verify(sessionService, times(1)).findById(1L);
    }

    @Test
    void testShowCreateForm_ServiceException_RedirectsWithError() throws Exception {
        when(trainingService.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/sessions/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"));

        verify(trainingService, times(1)).findAll();
    }

    @Test
    void testShowCreateForm_Success_ShowsForm() throws Exception {
        when(trainingService.findAll()).thenReturn(List.of(testTraining));
        when(employeeService.findAll()).thenReturn(List.of(testEmployee));

        mockMvc.perform(get("/sessions/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/form"))
                .andExpect(model().attributeExists("trainingSession"))
                .andExpect(model().attributeExists("trainings"))
                .andExpect(model().attributeExists("employees"));

        verify(trainingService, times(1)).findAll();
        verify(employeeService, times(1)).findAll();
    }

    // ==================== UPCOMING SESSIONS TESTS ====================

    @Test
    void testUpcoming_ExceptionThrown_ShowsErrorMessage() throws Exception {
        when(sessionService.findUpcoming()).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/sessions/upcoming"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/upcoming"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attributeExists("sessions"))
                .andExpect(model().attribute("sessions", Collections.emptyList()));

        verify(sessionService, times(1)).findUpcoming();
    }

    @Test
    void testUpcoming_Success_ShowsUpcomingSessions() throws Exception {
        when(sessionService.findUpcoming()).thenReturn(List.of(testSession));

        mockMvc.perform(get("/sessions/upcoming"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/upcoming"))
                .andExpect(model().attributeExists("sessions"))
                .andExpect(model().attribute("sessions", List.of(testSession)));

        verify(sessionService, times(1)).findUpcoming();
    }
}
