package york.fse.budgetappbackend.dto;

import java.math.BigDecimal;

public class CategorySpendingDTO {
    private String category;
    private BigDecimal totalAmount;
    private Long transactionCount;

    public CategorySpendingDTO() {}

    public CategorySpendingDTO(String category, BigDecimal totalAmount, Long transactionCount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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