package com.example.ai_expense_spliter.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_expense_spliter.dto.SettlementResponseDTO;
import com.example.ai_expense_spliter.dto.UserBalanceDTO;
import com.example.ai_expense_spliter.model.Expense;
import com.example.ai_expense_spliter.service.GeminiAIService;
import com.example.ai_expense_spliter.service.SettlementService;

@RestController
@RequestMapping("/api/settle")
public class SettlementController {
    
    @Autowired
    private SettlementService settlementService;
    
    @GetMapping("/{groupId}")
    public ResponseEntity<SettlementResponseDTO> getSettlementPlan(@PathVariable Long groupId) {
        SettlementResponseDTO settlement = settlementService.getSettlementPlan(groupId);
        return ResponseEntity.ok(settlement);
    }
    
    @GetMapping("/{groupId}/balances")
    public ResponseEntity<List<UserBalanceDTO>> getUserBalances(@PathVariable Long groupId) {
        List<UserBalanceDTO> balances = settlementService.getUserBalances(groupId);
        return ResponseEntity.ok(balances);
    }
    
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
    
    @GetMapping("/{groupId}/suggest-payer")
    public ResponseEntity<Map<String, String>> suggestNextPayer(@PathVariable Long groupId) {
        String suggestion = settlementService.suggestNextPayer(groupId);
        return ResponseEntity.ok(Map.of("suggestion", suggestion));
    }
    
    @GetMapping("/{groupId}/insights")
    public ResponseEntity<Map<String, Object>> getSpendingInsights(@PathVariable Long groupId) {
        Map<String, Object> insights = settlementService.getSpendingInsights(groupId);
        return ResponseEntity.ok(insights);
    }
    @Autowired
private GeminiAIService geminiAIService;

/**
 * Get AI-powered smart insights
 */
@GetMapping("/{groupId}/ai/smart-insights")
public ResponseEntity<Map<String, Object>> getSmartInsights(@PathVariable Long groupId) {
    return ResponseEntity.ok(geminiAIService.getSmartInsights(groupId));
}

/**
 * Get personalized recommendations for a user
 */
@GetMapping("/{groupId}/ai/recommendations/{userId}")
public ResponseEntity<Map<String, Object>> getRecommendations(
        @PathVariable Long groupId, 
        @PathVariable Long userId) {
    return ResponseEntity.ok(geminiAIService.getPersonalizedRecommendations(groupId, userId));
}

/**
 * Analyze expense fairness
 */
@GetMapping("/{groupId}/ai/fairness-analysis")
public ResponseEntity<Map<String, Object>> analyzeFairness(@PathVariable Long groupId) {
    return ResponseEntity.ok(geminiAIService.analyzeFairness(groupId));
}

/**
 * Predict spending trends
 */
@GetMapping("/{groupId}/ai/predict-trends")
public ResponseEntity<Map<String, Object>> predictTrends(@PathVariable Long groupId) {
    return ResponseEntity.ok(geminiAIService.predictSpendingTrends(groupId));
}

/**
 * Get smart splitting advice for new expense
 */
@PostMapping("/{groupId}/ai/splitting-advice")
public ResponseEntity<Map<String, Object>> getSplittingAdvice(
        @PathVariable Long groupId, 
        @RequestBody Expense expense) {
    return ResponseEntity.ok(geminiAIService.getSmartSplittingAdvice(groupId, expense));
}
}