package york.fse.budgetappbackend.dto;

import java.math.BigDecimal;
import java.util.List;

public class BudgetResponseDTO {
    private Long id;
    private BigDecimal amount;
    private String timeframe;
    private List<String> categories;
    private BigDecimal actualSpend;
    private boolean enabled;


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public BigDecimal getActualSpend() {
        return actualSpend;
    }

    public void setActualSpend(BigDecimal actualSpend) {
        this.actualSpend = actualSpend;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
