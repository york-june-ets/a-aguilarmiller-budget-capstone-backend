package york.fse.budgetappbackend.dto;

import java.math.BigDecimal;

public class MonthlySpendingComparisonDTO {
    private BigDecimal currentMonthSpending;
    private BigDecimal previousMonthSpending;

    public MonthlySpendingComparisonDTO() {}

    public MonthlySpendingComparisonDTO(BigDecimal currentMonthSpending, BigDecimal previousMonthSpending) {
        this.currentMonthSpending = currentMonthSpending != null ? currentMonthSpending : BigDecimal.ZERO;
        this.previousMonthSpending = previousMonthSpending != null ? previousMonthSpending : BigDecimal.ZERO;
    }

    public BigDecimal getCurrentMonthSpending() {
        return currentMonthSpending;
    }

    public void setCurrentMonthSpending(BigDecimal currentMonthSpending) {
        this.currentMonthSpending = currentMonthSpending;
    }

    public BigDecimal getPreviousMonthSpending() {
        return previousMonthSpending;
    }

    public void setPreviousMonthSpending(BigDecimal previousMonthSpending) {
        this.previousMonthSpending = previousMonthSpending;
    }
}