package york.fse.budgetappbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import york.fse.budgetappbackend.dto.MonthlySpendingComparisonDTO;
import york.fse.budgetappbackend.service.SpendingService;

@RestController
@RequestMapping("/api/spending")
public class SpendingController {

    private final SpendingService spendingService;

    public SpendingController(SpendingService spendingService) {
        this.spendingService = spendingService;
    }

    @GetMapping("/monthly-comparison/{userId}")
    public ResponseEntity<MonthlySpendingComparisonDTO> getMonthlyComparison(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer currentMonth,
            @RequestParam(required = false) Integer currentYear
    ) {
        MonthlySpendingComparisonDTO comparison = spendingService
                .getMonthlySpendingComparison(userId, currentMonth, currentYear);
        return ResponseEntity.ok(comparison);
    }
}