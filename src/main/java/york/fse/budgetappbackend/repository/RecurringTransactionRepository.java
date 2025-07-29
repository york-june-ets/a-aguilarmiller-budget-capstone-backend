package york.fse.budgetappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import york.fse.budgetappbackend.model.RecurringTransaction;

import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUserId(Long userId);
}
