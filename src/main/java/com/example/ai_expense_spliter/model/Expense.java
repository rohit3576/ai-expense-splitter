package com.example.ai_expense_spliter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String description;
    private BigDecimal amount;
    private LocalDateTime date;
    
    @ManyToOne
    @JoinColumn(name = "paid_by")
    private User paidBy;
    
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    private List<Split> splits;
    
    // Constructors
    public Expense() {}
    
    public Expense(String description, BigDecimal amount, User paidBy, Group group) {
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.group = group;
        this.date = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    
    public User getPaidBy() { return paidBy; }
    public void setPaidBy(User paidBy) { this.paidBy = paidBy; }
    
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    
    public List<Split> getSplits() { return splits; }
    public void setSplits(List<Split> splits) { this.splits = splits; }
}