package york.fse.budgetappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import york.fse.budgetappbackend.model.Transaction;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Get all transactions for a specific user
    List<Transaction> findAllByUserIdOrderByDateDesc(Long userId);

    // Filter by account
    List<Transaction> findAllByUserIdAndAccountIdOrderByDateDesc(Long userId, Long accountId);

    // Filter by a category name in the categories list
    @Query("SELECT t FROM Transaction t JOIN t.categories c WHERE t.user.id = :userId AND c = :category ORDER BY t.date DESC")
    List<Transaction> findAllByUserIdAndCategoryName(
            @Param("userId") Long userId,
            @Param("category") String category
    );
}
