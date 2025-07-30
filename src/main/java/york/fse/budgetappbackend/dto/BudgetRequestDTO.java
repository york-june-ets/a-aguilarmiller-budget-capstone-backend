package york.fse.budgetappbackend.dto;

import java.math.BigDecimal;
import java.util.List;

public class BudgetRequestDTO {
    private BigDecimal amount;
    private String timeframe;
    private List<String> categories;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
