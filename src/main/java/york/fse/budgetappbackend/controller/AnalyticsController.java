package york.fse.budgetappbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import york.fse.budgetappbackend.dto.CategorySpendingDTO;
import york.fse.budgetappbackend.dto.MonthlySpendingDTO;
import york.fse.budgetappbackend.service.AnalyticsService;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/user/{userId}/top-spending-categories")
    public ResponseEntity<List<CategorySpendingDTO>> getTopSpendingCategories(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        List<CategorySpendingDTO> categories = analyticsService.getTopSpendingCategories(userId, limit);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/user/{userId}/monthly-spending")
    public ResponseEntity<List<MonthlySpendingDTO>> getMonthlySpending(@PathVariable Long userId) {
        List<MonthlySpendingDTO> monthlyData = analyticsService.getMonthlySpending(userId);
        return ResponseEntity.ok(monthlyData);
    }
}