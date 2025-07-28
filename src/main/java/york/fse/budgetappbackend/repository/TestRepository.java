package york.fse.budgetappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import york.fse.budgetappbackend.model.TestEntity;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
}