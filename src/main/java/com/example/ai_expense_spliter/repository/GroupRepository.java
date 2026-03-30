package com.example.ai_expense_spliter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ai_expense_spliter.model.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
}