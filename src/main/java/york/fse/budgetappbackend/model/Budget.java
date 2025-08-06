package york.fse.budgetappbackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String timeframe;

    @ElementCollection
    @CollectionTable(
            name = "budget_categories",
            joinColumns = @JoinColumn(name = "budget_id")
    )

    @Column(name = "category")
    private List<String> categories = new ArrayList<>();

    @Column(name = "actual_spend", nullable = false)
    private BigDecimal actualSpend = BigDecimal.ZERO;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTimeframe() { return timeframe; }

    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public List<String> getCategories() { return categories; }

    public void setCategories(List<String> categories) { this.categories = categories; }

    public BigDecimal getActualSpend() { return actualSpend; }

    public void setActualSpend(BigDecimal actualSpend) { this.actualSpend = actualSpend; }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
