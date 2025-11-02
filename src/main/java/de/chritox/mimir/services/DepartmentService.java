package de.chritox.mimir.services;

import de.chritox.mimir.models.Department;
import de.chritox.mimir.repositories.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    @Transactional
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteById(Long id) {
        departmentRepository.deleteById(id);
    }
}
