package de.chritox.mimir.config;

import de.chritox.mimir.models.Employee;
import de.chritox.mimir.models.Training;
import de.chritox.mimir.services.EmployeeService;
import de.chritox.mimir.services.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final TrainingService trainingService;
    private final EmployeeService employeeService;

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(new StringToTrainingConverter());
        registry.addConverter(new StringToEmployeeConverter());
    }

    private class StringToTrainingConverter implements Converter<String, Training> {
        @Override
        public Training convert(@NonNull String source) {
            try {
                Long id = Long.parseLong(source);
                return trainingService.findById(id).orElse(null);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    private class StringToEmployeeConverter implements Converter<String, Employee> {
        @Override
        public Employee convert(@NonNull String source) {
            try {
                Long id = Long.parseLong(source);
                return employeeService.findById(id).orElse(null);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
