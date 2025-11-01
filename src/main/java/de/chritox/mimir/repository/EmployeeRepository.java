package de.chritox.mimir.repository;

import de.chritox.mimir.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
    
    Optional<Employee> findByEmail(String email);
}
