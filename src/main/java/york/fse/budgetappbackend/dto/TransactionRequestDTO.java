package york.fse.budgetappbackend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionRequestDTO {
    private BigDecimal amount;
    private String description;
    private String type;
    private LocalDate date;
    private Long accountId;
    private List<String> categories;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
}
