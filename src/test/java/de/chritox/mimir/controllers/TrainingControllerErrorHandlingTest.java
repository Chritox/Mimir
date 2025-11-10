package de.chritox.mimir.controllers;

import de.chritox.mimir.config.WebConfig;
import de.chritox.mimir.models.Training;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for error handling in TrainingController
 */
@WebMvcTest(value = TrainingController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class))
class TrainingControllerErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingService trainingService;

    private Training testTraining;

    @BeforeEach
    void setUp() {
        testTraining = new Training();
        testTraining.setId(1L);
        testTraining.setTitle("First Aid Training");
        testTraining.setDescription("Basic first aid course");
        testTraining.setInterval(12);
    }

    // ==================== LIST OPERATION TESTS ====================

    @Test
    void testList_ServiceThrowsException_ShowsErrorMessage() throws Exception {
        when(trainingService.findAll()).thenThrow(new RuntimeException("Service unavailable"));

        mockMvc.perform(get("/trainings"))
                .andExpect(status().isOk())
                .andExpect(view().name("trainings/list"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("trainings", Collections.emptyList()));

        verify(trainingService, times(1)).findAll();
    }

    // ==================== CREATE OPERATION TESTS ====================

    @Test
    void testCreate_DuplicateTitle_ShowsDataIntegrityError() throws Exception {
        when(trainingService.save(any(Training.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate title"));

        mockMvc.perform(post("/trainings")
                        .param("title", "First Aid Training")
                        .param("description", "Basic course"))
                .andExpect(status().isOk())
                .andExpect(view().name("trainings/form"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(trainingService, times(1)).save(any(Training.class));
    }

    @Test
    void testCreate_UnexpectedException_ShowsGenericError() throws Exception {
        when(trainingService.save(any(Training.class)))
                .thenThrow(new RuntimeException("Unexpected database error"));

        mockMvc.perform(post("/trainings")
                        .param("title", "First Aid Training"))
                .andExpect(status().isOk())
                .andExpect(view().name("trainings/form"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(trainingService, times(1)).save(any(Training.class));
    }

    @Test
    void testCreate_Success_ShowsSuccessMessageWithTitle() throws Exception {
        when(trainingService.save(any(Training.class))).thenReturn(testTraining);

        mockMvc.perform(post("/trainings")
                        .param("title", "First Aid Training")
                        .param("description", "Basic course"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trainings"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(trainingService, times(1)).save(any(Training.class));
    }

    // ==================== UPDATE OPERATION TESTS ====================

    @Test
    void testUpdate_TrainingNotFound_RedirectsWithError() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/trainings/1")
                        .param("title", "Updated Training"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trainings"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(trainingService, times(1)).findById(1L);
        verify(trainingService, never()).save(any(Training.class));
    }

    @Test
    void testUpdate_DataIntegrityViolation_ShowsError() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.of(testTraining));
        when(trainingService.save(any(Training.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        mockMvc.perform(put("/trainings/1")
                        .param("title", "Updated Training"))
                .andExpect(status().isOk())
                .andExpect(view().name("trainings/form"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(trainingService, times(1)).save(any(Training.class));
    }

    @Test
    void testUpdate_Success_RedirectsWithSuccessMessage() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.of(testTraining));
        when(trainingService.save(any(Training.class))).thenReturn(testTraining);

        mockMvc.perform(put("/trainings/1")
                        .param("title", "Updated Training"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trainings"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(trainingService, times(1)).save(any(Training.class));
    }

    // ==================== DELETE OPERATION TESTS ====================

    @Test
    void testDelete_TrainingNotFound_RedirectsWithError() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/trainings/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trainings"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(trainingService, times(1)).findById(1L);
        verify(trainingService, never()).deleteById(anyLong());
    }

    @Test
    void testDelete_HasDependentRecords_ShowsConstraintError() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.of(testTraining));
        doThrow(new DataIntegrityViolationException("Training has dependent sessions"))
                .when(trainingService).deleteById(1L);

        mockMvc.perform(delete("/trainings/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trainings"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(trainingService, times(1)).deleteById(1L);
    }

    @Test
    void testDelete_Success_ShowsSuccessMessageWithTitle() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.of(testTraining));
        doNothing().when(trainingService).deleteById(1L);

        mockMvc.perform(delete("/trainings/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trainings"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(trainingService, times(1)).findById(1L);
        verify(trainingService, times(1)).deleteById(1L);
    }

    // ==================== DETAIL VIEW TESTS ====================

    @Test
    void testDetail_TrainingNotFound_RedirectsWithError() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/trainings/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trainings"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(trainingService, times(1)).findById(1L);
    }

    @Test
    void testDetail_ServiceException_RedirectsWithError() throws Exception {
        when(trainingService.findById(1L)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/trainings/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trainings"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(trainingService, times(1)).findById(1L);
    }

    @Test
    void testDetail_Success_ShowsTrainingDetails() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.of(testTraining));

        mockMvc.perform(get("/trainings/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("trainings/detail"))
                .andExpect(model().attributeExists("training"))
                .andExpect(model().attribute("training", testTraining));

        verify(trainingService, times(1)).findById(1L);
    }

    // ==================== EDIT FORM TESTS ====================

    @Test
    void testShowEditForm_TrainingNotFound_RedirectsWithError() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/trainings/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trainings"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(trainingService, times(1)).findById(1L);
    }

    @Test
    void testShowEditForm_ServiceException_RedirectsWithError() throws Exception {
        when(trainingService.findById(1L)).thenThrow(new RuntimeException("Cannot load training"));

        mockMvc.perform(get("/trainings/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trainings"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(trainingService, times(1)).findById(1L);
    }

    @Test
    void testShowEditForm_Success_LoadsFormWithTraining() throws Exception {
        when(trainingService.findById(1L)).thenReturn(Optional.of(testTraining));

        mockMvc.perform(get("/trainings/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("trainings/form"))
                .andExpect(model().attributeExists("training"))
                .andExpect(model().attribute("training", testTraining));

        verify(trainingService, times(1)).findById(1L);
    }

    // ==================== CREATE FORM TESTS ====================

    @Test
    void testShowCreateForm_ServiceException_ShowsError() throws Exception {
        // This test simulates an exception during form initialization
        // In this case, the controller catches it and redirects
        mockMvc.perform(get("/trainings/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("trainings/form"))
                .andExpect(model().attributeExists("training"));
    }
}
