package com.example.ai_expense_spliter.service;

import com.example.ai_expense_spliter.dto.*;
import com.example.ai_expense_spliter.model.*;
import com.example.ai_expense_spliter.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SettlementService {
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private SplitRepository splitRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Calculate balances for all users in a group
     */
    public Map<Long, BigDecimal> calculateBalances(Long groupId) {
        Map<Long, BigDecimal> balances = new HashMap<>();
        
        // Get all expenses for the group
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        
        for (Expense expense : expenses) {
            Long paidBy = expense.getPaidBy().getId();
            BigDecimal amount = expense.getAmount();
            
            // Add to payer's balance (they are owed money)
            balances.merge(paidBy, amount, BigDecimal::add);
            
            // Get all splits for this expense
            List<Split> splits = splitRepository.findByExpenseId(expense.getId());
            
            for (Split split : splits) {
                Long userId = split.getUser().getId();
                BigDecimal owedAmount = split.getAmount();
                
                // Subtract from user's balance (they owe money)
                balances.merge(userId, owedAmount.negate(), BigDecimal::add);
            }
        }
        
        // Round all balances to 2 decimal places
        balances.replaceAll((k, v) -> v.setScale(2, RoundingMode.HALF_UP));
        
        return balances;
    }
    
    /**
     * Get user-wise balance details with names
     */
    public List<UserBalanceDTO> getUserBalances(Long groupId) {
        Map<Long, BigDecimal> balances = calculateBalances(groupId);
        List<UserBalanceDTO> userBalances = new ArrayList<>();
        
        for (Map.Entry<Long, BigDecimal> entry : balances.entrySet()) {
            User user = userRepository.findById(entry.getKey())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            userBalances.add(new UserBalanceDTO(
                entry.getKey(),
                user.getName(),
                entry.getValue()
            ));
        }
        
        return userBalances;
    }
    
    /**
     * Minimize transactions using greedy algorithm
     * Returns list of transactions to settle all debts
     */
    public List<TransactionDTO> minimizeTransactions(List<UserBalanceDTO> balances) {
        List<TransactionDTO> transactions = new ArrayList<>();
        
        // Separate debtors (negative balance) and creditors (positive balance)
        List<UserBalanceDTO> debtors = balances.stream()
            .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) < 0)
            .sorted(Comparator.comparing(UserBalanceDTO::getBalance))
            .collect(Collectors.toList());
        
        List<UserBalanceDTO> creditors = balances.stream()
            .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(UserBalanceDTO::getBalance).reversed())
            .collect(Collectors.toList());
        
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            UserBalanceDTO debtor = debtors.get(i);
            UserBalanceDTO creditor = creditors.get(j);
            
            BigDecimal debtAmount = debtor.getBalance().abs();
            BigDecimal creditAmount = creditor.getBalance();
            BigDecimal settlementAmount = debtAmount.min(creditAmount);
            
            if (settlementAmount.compareTo(BigDecimal.ZERO) > 0) {
                transactions.add(new TransactionDTO(
                    debtor.getUserName(),
                    creditor.getUserName(),
                    settlementAmount
                ));
            }
            
            // Update balances
            debtor.setBalance(debtor.getBalance().add(settlementAmount));
            creditor.setBalance(creditor.getBalance().subtract(settlementAmount));
            
            // Move to next if balance is settled
            if (debtor.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                i++;
            }
            if (creditor.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                j++;
            }
        }
        
        return transactions;
    }
    
    /**
     * Get complete settlement plan for a group
     */
    @Transactional
    public SettlementResponseDTO getSettlementPlan(Long groupId) {
        // Get group details
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        
        // Calculate balances
        List<UserBalanceDTO> balances = getUserBalances(groupId);
        
        // Generate minimized transactions
        List<TransactionDTO> transactions = minimizeTransactions(balances);
        
        // Create balance summary
        Map<String, BigDecimal> balanceMap = balances.stream()
            .collect(Collectors.toMap(
                UserBalanceDTO::getUserName,
                UserBalanceDTO::getBalance
            ));
        
        BigDecimal totalSpent = balances.stream()
            .map(UserBalanceDTO::getBalance)
            .filter(b -> b.compareTo(BigDecimal.ZERO) > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BalanceSummaryDTO summary = new BalanceSummaryDTO(
            balanceMap,
            totalSpent,
            balances.size()
        );
        
        return new SettlementResponseDTO(
            groupId,
            group.getName(),
            transactions,
            summary
        );
    }
    
    /**
     * Get fairness score for a user in a group
     * Returns how fair the expense distribution is
     */
    public BigDecimal getFairnessScore(Long groupId, Long userId) {
        List<UserBalanceDTO> balances = getUserBalances(groupId);
        
        UserBalanceDTO userBalance = balances.stream()
            .filter(b -> b.getUserId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("User not found in group"));
        
        BigDecimal totalSpent = balances.stream()
            .map(UserBalanceDTO::getBalance)
            .filter(b -> b.compareTo(BigDecimal.ZERO) > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal fairShare = totalSpent.divide(BigDecimal.valueOf(balances.size()), 2, RoundingMode.HALF_UP);
        BigDecimal actualNet = userBalance.getBalance();
        
        // Score calculation: 100% if balanced, decreases as deviation increases
        BigDecimal deviation = actualNet.subtract(fairShare).abs();
        BigDecimal maxDeviation = totalSpent;
        
        if (maxDeviation.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        BigDecimal score = BigDecimal.valueOf(100)
            .subtract(deviation.divide(maxDeviation, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        
        return score.max(BigDecimal.ZERO);
    }
    
    /**
     * Suggest who should pay next
     * Returns the user who should pay based on current balances
     */
    public String suggestNextPayer(Long groupId) {
        List<UserBalanceDTO> balances = getUserBalances(groupId);
        
        // Find the user with the most negative balance (owes the most)
        UserBalanceDTO highestDebtor = balances.stream()
            .filter(b -> b.getBalance().compareTo(BigDecimal.ZERO) < 0)
            .min(Comparator.comparing(UserBalanceDTO::getBalance))
            .orElse(null);
        
        if (highestDebtor != null) {
            return highestDebtor.getUserName() + " should pay next as they owe the most (₹" + 
                   highestDebtor.getBalance().abs() + ")";
        }
        
        return "All balances are settled! Everyone is even.";
    }
    
    /**
     * Get spending insights for a group
     */
    public Map<String, Object> getSpendingInsights(Long groupId) {
        Map<String, Object> insights = new HashMap<>();
        
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        
        // Total expenses
        BigDecimal totalExpenses = expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Average expense per person
        int memberCount = groupRepository.findById(groupId).get().getMembers().size();
        BigDecimal avgPerPerson = totalExpenses.divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP);
        
        // Highest expense
        Optional<Expense> highestExpense = expenses.stream()
            .max(Comparator.comparing(Expense::getAmount));
        
        // Most active spender (who paid most)
        Map<Long, BigDecimal> totalPaid = new HashMap<>();
        for (Expense expense : expenses) {
            totalPaid.merge(expense.getPaidBy().getId(), expense.getAmount(), BigDecimal::add);
        }
        
        Optional<Map.Entry<Long, BigDecimal>> mostActive = totalPaid.entrySet().stream()
            .max(Map.Entry.comparingByValue());
        
        insights.put("totalExpenses", totalExpenses);
        insights.put("averagePerPerson", avgPerPerson);
        insights.put("numberOfExpenses", expenses.size());
        insights.put("highestExpense", highestExpense.map(e -> e.getDescription() + ": ₹" + e.getAmount()).orElse("N/A"));
        
        if (mostActive.isPresent()) {
            User user = userRepository.findById(mostActive.get().getKey()).orElse(null);
            insights.put("mostActiveSpender", user != null ? user.getName() + " (₹" + mostActive.get().getValue() + ")" : "N/A");
        }
        
        return insights;
    }
}