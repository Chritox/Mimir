package de.chritox.mimir.controllers;

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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingSessionController.class)
class TrainingSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingSessionService sessionService;

    @MockBean
    private TrainingService trainingService;

    @MockBean
    private EmployeeService employeeService;

    private Training testTraining;
    private Employee testEmployee;
    private TrainingSession testSession;

    @BeforeEach
    void setUp() {
        testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTitle("Test Schulung");
        testTraining.setDescription("Test Beschreibung");
        testTraining.setInterval(12);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Max Mustermann");
        testEmployee.setMandatoryTrainings(new HashSet<>());
        testEmployee.setAttendedSessions(new HashSet<>());

        testSession = new TrainingSession();
        testSession.setId(1L);
        testSession.setTraining(testTraining);
        testSession.setDate(LocalDate.now().plusDays(7));
        testSession.setParticipants(new HashSet<>());
    }

    @Test
    void testListSessions() throws Exception {
        when(sessionService.findAll()).thenReturn(List.of(testSession));

        mockMvc.perform(get("/sessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/list"))
                .andExpect(model().attributeExists("sessions"));

        verify(sessionService, times(1)).findAll();
    }

    @Test
    void testShowCreateForm() throws Exception {
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

    @Test
    void testCreateSession() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.of(testTraining));
        when(employeeService.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(sessionService.save(any(TrainingSession.class))).thenReturn(testSession);

        mockMvc.perform(post("/sessions")
                        .param("training", "1")
                        .param("date", LocalDate.now().plusDays(7).toString())
                        .param("participants", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"));

        verify(sessionService, times(1)).save(any(TrainingSession.class));
    }

    @Test
    void testShowEditForm() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.of(testSession));
        when(trainingService.findAll()).thenReturn(List.of(testTraining));
        when(employeeService.findAll()).thenReturn(List.of(testEmployee));

        mockMvc.perform(get("/sessions/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/form"))
                .andExpect(model().attributeExists("trainingSession"))
                .andExpect(model().attributeExists("trainings"))
                .andExpect(model().attributeExists("employees"));

        verify(sessionService, times(1)).findById(1L);
    }

    @Test
    void testDetailView() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.of(testSession));

        mockMvc.perform(get("/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/detail"))
                .andExpect(model().attributeExists("trainingSession"));

        verify(sessionService, times(1)).findById(1L);
    }

    @Test
    void testDeleteSession() throws Exception {
        when(sessionService.findById(1L)).thenReturn(Optional.of(testSession));
        doNothing().when(sessionService).deleteById(1L);

        mockMvc.perform(delete("/sessions/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions"));

        verify(sessionService, times(1)).findById(1L);
        verify(sessionService, times(1)).deleteById(1L);
    }

    @Test
    void testUpcomingSessions() throws Exception {
        when(sessionService.findUpcoming()).thenReturn(List.of(testSession));

        mockMvc.perform(get("/sessions/upcoming"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions/upcoming"))
                .andExpect(model().attributeExists("sessions"));

        verify(sessionService, times(1)).findUpcoming();
    }
}
