package york.fse.budgetappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import york.fse.budgetappbackend.model.Transaction;
import york.fse.budgetappbackend.dto.CategorySpendingDTO;
import york.fse.budgetappbackend.dto.MonthlySpendingDTO;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByUserIdOrderByDateDescIdDesc(Long userId);

    List<Transaction> findAllByUserIdAndAccountIdOrderByDateDescIdDesc(Long userId, Long accountId);

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t JOIN t.categories c WHERE t.user.id = :userId AND c = :category ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findAllByUserIdAndCategoryName(
            @Param("userId") Long userId,
            @Param("category") String category
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
            "AND EXTRACT(MONTH FROM t.date) = :month " +
            "AND EXTRACT(YEAR FROM t.date) = :year")
    BigDecimal getTotalExpensesByUserAndMonth(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("SELECT DISTINCT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.date >= :startDate AND t.date <= :endDate " +
            "AND t.account.id = :accountId " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithAllFilters(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("accountId") Long accountId,
            Pageable pageable
    );


    @Query("SELECT DISTINCT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.date >= :startDate AND t.date <= :endDate " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );


    @Query("SELECT DISTINCT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.date >= :startDate " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithStartDate(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            Pageable pageable
    );


    @Query("SELECT DISTINCT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.date <= :endDate " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithEndDate(
            @Param("userId") Long userId,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );


    @Query("SELECT DISTINCT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.account.id = :accountId " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdAndAccountId(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            Pageable pageable
    );


    @Query("SELECT DISTINCT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.date >= :startDate AND t.account.id = :accountId " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithStartDateAndAccount(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("accountId") Long accountId,
            Pageable pageable
    );


    @Query("SELECT DISTINCT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.date <= :endDate AND t.account.id = :accountId " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithEndDateAndAccount(
            @Param("userId") Long userId,
            @Param("endDate") LocalDate endDate,
            @Param("accountId") Long accountId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM Transaction t JOIN t.categories c WHERE t.user.id = :userId " +
            "AND t.date >= :startDate AND t.date <= :endDate " +
            "AND t.account.id = :accountId AND c IN :categories " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithAllFiltersAndCategories(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("accountId") Long accountId,
            @Param("categories") List<String> categories,
            Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM Transaction t JOIN t.categories c WHERE t.user.id = :userId " +
            "AND t.date >= :startDate AND t.date <= :endDate " +
            "AND c IN :categories " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithDateRangeAndCategories(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categories") List<String> categories,
            Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM Transaction t JOIN t.categories c WHERE t.user.id = :userId " +
            "AND t.date >= :startDate AND c IN :categories " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithStartDateAndCategories(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("categories") List<String> categories,
            Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM Transaction t JOIN t.categories c WHERE t.user.id = :userId " +
            "AND t.date <= :endDate AND c IN :categories " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithEndDateAndCategories(
            @Param("userId") Long userId,
            @Param("endDate") LocalDate endDate,
            @Param("categories") List<String> categories,
            Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM Transaction t JOIN t.categories c WHERE t.user.id = :userId " +
            "AND t.account.id = :accountId AND c IN :categories " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithAccountAndCategories(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            @Param("categories") List<String> categories,
            Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM Transaction t JOIN t.categories c WHERE t.user.id = :userId " +
            "AND c IN :categories " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithCategories(
            @Param("userId") Long userId,
            @Param("categories") List<String> categories,
            Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM Transaction t JOIN t.categories c WHERE t.user.id = :userId " +
            "AND t.date >= :startDate AND t.account.id = :accountId " +
            "AND c IN :categories " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithStartDateAccountAndCategories(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("accountId") Long accountId,
            @Param("categories") List<String> categories,
            Pageable pageable
    );

    @Query("SELECT DISTINCT t FROM Transaction t JOIN t.categories c WHERE t.user.id = :userId " +
            "AND t.date <= :endDate AND t.account.id = :accountId " +
            "AND c IN :categories " +
            "ORDER BY t.date DESC, t.id DESC")
    Page<Transaction> findByUserIdWithEndDateAccountAndCategories(
            @Param("userId") Long userId,
            @Param("endDate") LocalDate endDate,
            @Param("accountId") Long accountId,
            @Param("categories") List<String> categories,
            Pageable pageable
    );

    @Query(value = """
    SELECT tc.categories as category,
           SUM(t.amount) as total_amount,
           COUNT(*) as transaction_count
    FROM transactions t 
    JOIN transaction_categories tc ON t.id = tc.transaction_id
    WHERE t.user_id = :userId AND t.type = 'EXPENSE'
    GROUP BY tc.categories
    ORDER BY total_amount DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findTopSpendingCategoriesRaw(@Param("userId") Long userId, @Param("limit") int limit);

    @Query(value = """
    SELECT TO_CHAR(t.date, 'YYYY-MM') as month,
           SUM(t.amount) as total_amount,
           COUNT(DISTINCT t.id) as transaction_count
    FROM transactions t 
    JOIN transaction_categories tc ON t.id = tc.transaction_id
    WHERE t.user_id = :userId AND t.type = 'EXPENSE'
    GROUP BY TO_CHAR(t.date, 'YYYY-MM')
    ORDER BY month DESC
    """, nativeQuery = true)
    List<Object[]> findMonthlySpendingRaw(@Param("userId") Long userId);

    default List<CategorySpendingDTO> findTopSpendingCategories(Long userId, int limit) {
        return findTopSpendingCategoriesRaw(userId, limit).stream()
                .map(row -> new CategorySpendingDTO(
                        (String) row[0],
                        (java.math.BigDecimal) row[1],
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    default List<MonthlySpendingDTO> findMonthlySpending(Long userId) {
        return findMonthlySpendingRaw(userId).stream()
                .map(row -> new MonthlySpendingDTO(
                        (String) row[0],
                        (java.math.BigDecimal) row[1],
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }
}