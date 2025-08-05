package york.fse.budgetappbackend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RecurringTransactionResponseDTO {

    private Long id;
    private Long userId;
    private Long accountId;
    private String accountName;
    private String accountNumber;
    private String description;
    private BigDecimal amount;
    private String frequency;
    private LocalDate startDate;
    private LocalDate nextDate;
    private List<String> categories;
    private LocalDate lastGeneratedDate;

    public RecurringTransactionResponseDTO(
            Long id,
            Long userId,
            Long accountId,
            String accountName,
            String accountNumber,
            String description,
            BigDecimal amount,
            String frequency,
            LocalDate startDate,
            LocalDate nextDate,
            List<String> categories,
            LocalDate lastGeneratedDate
    ) {
        this.id = id;
        this.userId = userId;
        this.accountId = accountId;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.description = description;
        this.amount = amount;
        this.frequency = frequency;
        this.startDate = startDate;
        this.nextDate = nextDate;
        this.categories = categories;
        this.lastGeneratedDate = lastGeneratedDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getNextDate() {
        return nextDate;
    }

    public void setNextDate(LocalDate nextDate) {
        this.nextDate = nextDate;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public LocalDate getLastGeneratedDate() {
        return lastGeneratedDate;
    }

    public void setLastGeneratedDate(LocalDate lastGeneratedDate) {
        this.lastGeneratedDate = lastGeneratedDate;
    }
}
