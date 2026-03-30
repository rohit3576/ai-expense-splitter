package com.example.ai_expense_spliter.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "splits")
public class Split {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal amount;
    
    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    // Constructors
    public Split() {}
    
    public Split(BigDecimal amount, Expense expense, User user) {
        this.amount = amount;
        this.expense = expense;
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}