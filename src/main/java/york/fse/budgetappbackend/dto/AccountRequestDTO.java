package york.fse.budgetappbackend.dto;

import java.math.BigDecimal;

public class AccountRequestDTO {
    private Long userId;
    private String name;
    private String type;
    private BigDecimal balance;


    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}