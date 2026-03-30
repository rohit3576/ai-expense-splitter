package com.example.ai_expense_spliter.dto;

import java.util.List;

public class SettlementResponseDTO {
    private Long groupId;
    private String groupName;
    private List<TransactionDTO> transactions;
    private BalanceSummaryDTO summary;
    
    // Constructors
    public SettlementResponseDTO() {}
    
    public SettlementResponseDTO(Long groupId, String groupName, 
                                 List<TransactionDTO> transactions, 
                                 BalanceSummaryDTO summary) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.transactions = transactions;
        this.summary = summary;
    }
    
    // Getters and Setters
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    
    public List<TransactionDTO> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionDTO> transactions) { this.transactions = transactions; }
    
    public BalanceSummaryDTO getSummary() { return summary; }
    public void setSummary(BalanceSummaryDTO summary) { this.summary = summary; }
}