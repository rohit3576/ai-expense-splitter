package com.example.ai_expense_spliter.dto;

import java.math.BigDecimal;

public class UserBalanceDTO implements Comparable<UserBalanceDTO> {
    private Long userId;
    private String userName;
    private BigDecimal balance;
    
    public UserBalanceDTO() {}
    
    public UserBalanceDTO(Long userId, String userName, BigDecimal balance) {
        this.userId = userId;
        this.userName = userName;
        this.balance = balance;
    }
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    @Override
    public int compareTo(UserBalanceDTO other) {
        return this.balance.compareTo(other.balance);
    }
}