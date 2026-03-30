package com.example.ai_expense_spliter.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ai_expense_spliter.model.Expense;
import com.example.ai_expense_spliter.model.Split;
import com.example.ai_expense_spliter.repository.ExpenseRepository;
import com.example.ai_expense_spliter.repository.GroupRepository;
import com.example.ai_expense_spliter.repository.SplitRepository;
import com.example.ai_expense_spliter.repository.UserRepository;

@Service
public class ExpenseService {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private SplitRepository splitRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Transactional
    public Expense createExpense(Expense expense) {
        // Set the date if not set
        if (expense.getDate() == null) {
            expense.setDate(LocalDateTime.now());
        }
        
        // Save the expense first
        Expense savedExpense = expenseRepository.save(expense);
        
        // Save all splits
        if (expense.getSplits() != null) {
            for (Split split : expense.getSplits()) {
                split.setExpense(savedExpense);
                splitRepository.save(split);
            }
        }
        
        return savedExpense;
    }
    
    public List<Expense> getExpensesByGroup(Long groupId) {
        return expenseRepository.findByGroupId(groupId);
    }
}