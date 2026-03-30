package com.example.ai_expense_spliter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ai_expense_spliter.model.Split;

public interface SplitRepository extends JpaRepository<Split, Long> {
    List<Split> findByExpenseId(Long expenseId);
}