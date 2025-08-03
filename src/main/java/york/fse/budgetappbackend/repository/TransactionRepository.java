package york.fse.budgetappbackend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import york.fse.budgetappbackend.model.Transaction;

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
}