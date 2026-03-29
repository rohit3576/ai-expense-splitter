package com.example.ai_expense_spliter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ai_expense_spliter.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}