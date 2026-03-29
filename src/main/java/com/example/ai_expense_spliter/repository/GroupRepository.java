package com.example.ai_expense_spliter.repository;

import com.example.ai_expense_spliter.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}