package york.fse.budgetappbackend.dto;

import java.math.BigDecimal;

public class MonthlySpendingDTO {
    private String month;
    private BigDecimal totalAmount;
    private Long transactionCount;

    public MonthlySpendingDTO() {}

    public MonthlySpendingDTO(String month, BigDecimal totalAmount, Long transactionCount) {
        this.month = month;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }
}