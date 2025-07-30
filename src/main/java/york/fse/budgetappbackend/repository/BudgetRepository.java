package york.fse.budgetappbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import york.fse.budgetappbackend.model.Budget;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdAndEnabledTrue(Long userId);

    List<Budget> findAllByUserId(Long userId);


    List<Budget> findAllByUserIdAndTimeframe(Long userId, String timeframe);

    @Query("SELECT b FROM Budget b JOIN b.categories c " +
            "WHERE b.user.id = :userId AND c = :category")
    List<Budget> findAllByUserIdAndCategory(
            @Param("userId") Long userId,
            @Param("category") String category
    );

    @Query("SELECT COUNT(b) > 0 FROM Budget b JOIN b.categories c " +
            "WHERE b.user.id = :userId AND c = :category AND b.timeframe = :timeframe")
    boolean existsByUserIdAndCategoryAndTimeframe(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("timeframe") String timeframe
    );
}
