package com.example.ai_expense_spliter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_expense_spliter.model.Expense;
import com.example.ai_expense_spliter.service.ExpenseService;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    
    @Autowired
    private ExpenseService expenseService;
    
    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense) {
        return ResponseEntity.ok(expenseService.createExpense(expense));
    }
    
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Expense>> getExpensesByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getExpensesByGroup(groupId));
    }
}