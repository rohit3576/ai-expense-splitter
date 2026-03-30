package com.example.ai_expense_spliter.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_expense_spliter.dto.SettlementResponseDTO;
import com.example.ai_expense_spliter.dto.UserBalanceDTO;
import com.example.ai_expense_spliter.service.SettlementService;

@RestController
@RequestMapping("/api/settle")
public class SettlementController {
    
    @Autowired
    private SettlementService settlementService;
    
    /**
     * Get settlement plan for a group
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<SettlementResponseDTO> getSettlementPlan(@PathVariable Long groupId) {
        SettlementResponseDTO settlement = settlementService.getSettlementPlan(groupId);
        return ResponseEntity.ok(settlement);
    }
    
    /**
     * Get all user balances in a group
     */
    @GetMapping("/{groupId}/balances")
    public ResponseEntity<List<UserBalanceDTO>> getUserBalances(@PathVariable Long groupId) {
        List<UserBalanceDTO> balances = settlementService.getUserBalances(groupId);
        return ResponseEntity.ok(balances);
    }
    
    /**
     * Get fairness score for a user in a group
     */
    @GetMapping("/{groupId}/fairness/{userId}")
    public ResponseEntity<Map<String, Object>> getFairnessScore(
            @PathVariable Long groupId, 
            @PathVariable Long userId) {
        BigDecimal score = settlementService.getFairnessScore(groupId, userId);
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "fairnessScore", score,
            "message", "Fairness score out of 100"
        ));
    }
    
    /**
     * Get suggestion for next payer
     */
    @GetMapping("/{groupId}/suggest-payer")
    public ResponseEntity<Map<String, String>> suggestNextPayer(@PathVariable Long groupId) {
        String suggestion = settlementService.suggestNextPayer(groupId);
        return ResponseEntity.ok(Map.of("suggestion", suggestion));
    }
    
    /**
     * Get spending insights for a group
     */
    @GetMapping("/{groupId}/insights")
    public ResponseEntity<Map<String, Object>> getSpendingInsights(@PathVariable Long groupId) {
        Map<String, Object> insights = settlementService.getSpendingInsights(groupId);
        return ResponseEntity.ok(insights);
    }
}