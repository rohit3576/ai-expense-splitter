package com.example.ai_expense_spliter.dto;

import java.math.BigDecimal;
import java.util.Map;

public class BalanceSummaryDTO {
    private Map<String, BigDecimal> balances;
    private BigDecimal totalSpent;
    private int numberOfParticipants;
    
    public BalanceSummaryDTO() {}
    
    public BalanceSummaryDTO(Map<String, BigDecimal> balances, 
                            BigDecimal totalSpent, 
                            int numberOfParticipants) {
        this.balances = balances;
        this.totalSpent = totalSpent;
        this.numberOfParticipants = numberOfParticipants;
    }
    
    // Getters and Setters
    public Map<String, BigDecimal> getBalances() { return balances; }
    public void setBalances(Map<String, BigDecimal> balances) { this.balances = balances; }
    
    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
    
    public int getNumberOfParticipants() { return numberOfParticipants; }
    public void setNumberOfParticipants(int numberOfParticipants) { this.numberOfParticipants = numberOfParticipants; }
}