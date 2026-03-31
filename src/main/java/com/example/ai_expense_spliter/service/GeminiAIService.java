package com.example.ai_expense_spliter.service;

import com.example.ai_expense_spliter.model.Expense;
import com.example.ai_expense_spliter.model.User;
import com.example.ai_expense_spliter.repository.ExpenseRepository;
import com.example.ai_expense_spliter.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiAIService {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get AI-powered spending insights using Gemini
     */
    @Cacheable(value = "gemini-insights", key = "#groupId")
    public Map<String, Object> getSmartInsights(Long groupId) {
        Map<String, Object> insights = new HashMap<>();
        
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        
        if (expenses.isEmpty()) {
            insights.put("message", "Add some expenses to get AI insights!");
            insights.put("tip", "Try adding a few expenses to see smart analysis");
            return insights;
        }
        
        try {
            // Prepare expense data
            String expenseData = prepareExpenseDataForAI(expenses, groupId);
            
            // Create prompt for Gemini
            String prompt = createInsightPrompt(expenseData);
            
            // Call Gemini API
            String aiResponse = callGeminiAPI(prompt);
            
            // Parse and structure the response
            insights.put("aiAnalysis", aiResponse);
            insights.put("summary", getQuickSummary(expenses));
            insights.put("recommendations", extractRecommendations(aiResponse));
            insights.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            insights.put("error", "AI service temporarily unavailable");
            insights.put("fallbackInsights", getFallbackInsights(expenses));
            insights.put("tip", "Try again later for AI-powered insights");
        }
        
        return insights;
    }
    
    /**
     * Get personalized spending recommendations
     */
    public Map<String, Object> getPersonalizedRecommendations(Long groupId, Long userId) {
        Map<String, Object> recommendations = new HashMap<>();
        
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        User user = userRepository.findById(userId).orElse(null);
        
        if (user == null || expenses.isEmpty()) {
            recommendations.put("message", "Not enough data for recommendations");
            return recommendations;
        }
        
        try {
            // Calculate user's spending stats
            BigDecimal userTotalPaid = expenses.stream()
                .filter(e -> e.getPaidBy().getId().equals(userId))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalGroupSpending = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double percentage = userTotalPaid.doubleValue() / totalGroupSpending.doubleValue() * 100;
            
            // Create personalized prompt
            String prompt = String.format(
                "As a financial advisor, analyze this user's spending pattern:\n\n" +
                "User: %s\n" +
                "Total amount paid: ₹%s\n" +
                "Group total: ₹%s\n" +
                "Percentage of total: %.1f%%\n" +
                "Number of expenses paid: %d\n\n" +
                "Provide 3 specific, actionable recommendations for this user to manage their shared expenses better. " +
                "Keep it friendly and practical. Format as bullet points.",
                user.getName(),
                userTotalPaid,
                totalGroupSpending,
                percentage,
                expenses.stream().filter(e -> e.getPaidBy().getId().equals(userId)).count()
            );
            
            String aiResponse = callGeminiAPI(prompt);
            
            recommendations.put("userName", user.getName());
            recommendations.put("spendingStats", Map.of(
                "totalPaid", userTotalPaid,
                "percentageOfTotal", Math.round(percentage),
                "expensesPaid", expenses.stream().filter(e -> e.getPaidBy().getId().equals(userId)).count()
            ));
            recommendations.put("aiRecommendations", aiResponse);
            recommendations.put("quickTips", getQuickTips(userTotalPaid, totalGroupSpending));
            
        } catch (Exception e) {
            recommendations.put("message", "AI recommendations temporarily unavailable");
            recommendations.put("basicAdvice", "Try to maintain balanced contributions with your group members");
        }
        
        return recommendations;
    }
    
    /**
     * Analyze expense fairness using AI
     */
    public Map<String, Object> analyzeFairness(Long groupId) {
        Map<String, Object> fairness = new HashMap<>();
        
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        
        if (expenses.isEmpty()) {
            fairness.put("message", "No expenses to analyze");
            return fairness;
        }
        
        try {
            // Calculate per-user spending
            Map<String, BigDecimal> userSpending = new HashMap<>();
            for (Expense expense : expenses) {
                String userName = expense.getPaidBy().getName();
                userSpending.merge(userName, expense.getAmount(), BigDecimal::add);
            }
            
            // Create fairness analysis prompt
            StringBuilder spendingData = new StringBuilder();
            for (Map.Entry<String, BigDecimal> entry : userSpending.entrySet()) {
                spendingData.append(String.format("- %s: ₹%s\n", entry.getKey(), entry.getValue()));
            }
            
            String prompt = String.format(
                "Analyze the fairness of expense distribution in a group:\n\n" +
                "Spending by each person:\n%s\n" +
                "Total expenses: ₹%s\n" +
                "Number of people: %d\n\n" +
                "Answer these questions:\n" +
                "1. Is the spending distribution fair? Why or why not?\n" +
                "2. Who should pay next to balance things?\n" +
                "3. What's the ideal fair share per person?\n\n" +
                "Provide a concise, helpful analysis.",
                spendingData.toString(),
                expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                userSpending.size()
            );
            
            String aiResponse = callGeminiAPI(prompt);
            
            fairness.put("aiAnalysis", aiResponse);
            fairness.put("spendingBreakdown", userSpending);
            fairness.put("fairnessScore", calculateFairnessScore(userSpending));
            
        } catch (Exception e) {
            fairness.put("message", "AI fairness analysis unavailable");
            fairness.put("basicAnalysis", "Check if everyone is contributing proportionally");
        }
        
        return fairness;
    }
    
    /**
     * Predict future spending patterns
     */
    public Map<String, Object> predictSpendingTrends(Long groupId) {
        Map<String, Object> prediction = new HashMap<>();
        
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        
        if (expenses.size() < 3) {
            prediction.put("message", "Need at least 3 expenses for trend prediction");
            return prediction;
        }
        
        try {
            // Calculate monthly trends
            Map<String, BigDecimal> monthlySpending = new TreeMap<>();
            for (Expense expense : expenses) {
                String month = expense.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthlySpending.merge(month, expense.getAmount(), BigDecimal::add);
            }
            
            // Create prediction prompt
            StringBuilder trendData = new StringBuilder();
            for (Map.Entry<String, BigDecimal> entry : monthlySpending.entrySet()) {
                trendData.append(String.format("- %s: ₹%s\n", entry.getKey(), entry.getValue()));
            }
            
            String prompt = String.format(
                "Based on these monthly spending patterns:\n%s\n\n" +
                "Predict the spending trend for the next month. Consider:\n" +
                "1. Is spending increasing or decreasing?\n" +
                "2. What might be the estimated amount for next month?\n" +
                "3. Any patterns you notice?\n\n" +
                "Provide a brief prediction.",
                trendData.toString()
            );
            
            String aiResponse = callGeminiAPI(prompt);
            
            prediction.put("aiPrediction", aiResponse);
            prediction.put("historicalData", monthlySpending);
            prediction.put("trend", calculateTrend(monthlySpending));
            prediction.put("estimatedNextMonth", estimateNextMonth(monthlySpending));
            
        } catch (Exception e) {
            prediction.put("message", "Prediction service unavailable");
            prediction.put("simpleEstimate", "Based on average: ₹" + getAverageExpense(expenses));
        }
        
        return prediction;
    }
    
    /**
     * Get smart expense splitting suggestions
     */
    public Map<String, Object> getSmartSplittingAdvice(Long groupId, Expense newExpense) {
        Map<String, Object> advice = new HashMap<>();
        
        List<Expense> pastExpenses = expenseRepository.findByGroupId(groupId);
        List<User> groupMembers = getGroupMembers(groupId);
        
        try {
            String prompt = String.format(
                "Suggest a fair way to split this expense: ₹%s for '%s'\n" +
                "Group members: %s\n" +
                "Past expenses: %d previous transactions\n\n" +
                "Consider: equal split, percentage-based, or custom split based on past patterns. " +
                "Provide reasoning.",
                newExpense.getAmount(),
                newExpense.getDescription(),
                groupMembers.stream().map(User::getName).collect(Collectors.joining(", ")),
                pastExpenses.size()
            );
            
            String aiResponse = callGeminiAPI(prompt);
            
            advice.put("aiSuggestion", aiResponse);
            advice.put("suggestedSplit", getSuggestedSplit(groupMembers, newExpense.getAmount()));
            advice.put("reasoning", "Based on past spending patterns and fairness principles");
            
        } catch (Exception e) {
            advice.put("defaultSplit", "Equal split recommended");
            advice.put("amountPerPerson", newExpense.getAmount().divide(BigDecimal.valueOf(groupMembers.size()), 2, RoundingMode.HALF_UP));
        }
        
        return advice;
    }
    
    // Helper Methods
    
    private String prepareExpenseDataForAI(List<Expense> expenses, Long groupId) {
        StringBuilder data = new StringBuilder();
        data.append("Expense History:\n");
        
        // Group by category
        Map<String, List<Expense>> byCategory = expenses.stream()
            .collect(Collectors.groupingBy(e -> categorizeExpense(e.getDescription())));
        
        for (Map.Entry<String, List<Expense>> entry : byCategory.entrySet()) {
            BigDecimal total = entry.getValue().stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            data.append(String.format("- %s: %d expenses, total ₹%s\n", 
                entry.getKey(), entry.getValue().size(), total));
        }
        
        // Add top spenders
        Map<String, BigDecimal> topSpenders = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> e.getPaidBy().getName(),
                Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
            ));
        
        data.append("\nTop Contributors:\n");
        topSpenders.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
            .limit(3)
            .forEach(e -> data.append(String.format("- %s: ₹%s\n", e.getKey(), e.getValue())));
        
        return data.toString();
    }
    
    private String createInsightPrompt(String expenseData) {
        return String.format(
            "You are a financial advisor for a group expense sharing app. " +
            "Analyze this spending data and provide insights:\n\n%s\n\n" +
            "Provide:\n" +
            "1. Key spending pattern (2-3 sentences)\n" +
            "2. 2-3 practical recommendations for better expense management\n" +
            "3. A friendly tip for the group\n\n" +
            "Keep it concise, helpful, and actionable.",
            expenseData
        );
    }
    
    private String callGeminiAPI(String prompt) {
        try {
            WebClient client = webClientBuilder.build();
            
            // Create request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = objectMapper.createArrayNode();
            ObjectNode content = objectMapper.createObjectNode();
            ArrayNode parts = objectMapper.createArrayNode();
            ObjectNode part = objectMapper.createObjectNode();
            part.put("text", prompt);
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
            requestBody.set("contents", contents);
            
            // Make API call
            String response = client.post()
                .uri(apiUrl + "?key=" + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            // Parse response
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.get("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonNode textPart = candidates.get(0)
                    .get("content")
                    .get("parts")
                    .get(0)
                    .get("text");
                if (textPart != null) {
                    return textPart.asText();
                }
            }
            
            return "AI analysis complete. Based on your spending patterns, consider setting a group budget and tracking expenses regularly.";
            
        } catch (Exception e) {
            e.printStackTrace();
            return getFallbackResponse();
        }
    }
    
    private String getFallbackResponse() {
        return "💡 Smart Tip: Regular expense tracking helps maintain fair contributions. " +
               "Consider setting up a group budget and reviewing expenses weekly!";
    }
    
    private Map<String, Object> getQuickSummary(List<Expense> expenses) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalExpenses", expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.put("averageExpense", expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(expenses.size()), 2, RoundingMode.HALF_UP));
        summary.put("mostCommonCategory", getMostCommonCategory(expenses));
        return summary;
    }
    
    private List<String> extractRecommendations(String aiResponse) {
        List<String> recommendations = new ArrayList<>();
        // Simple extraction - look for numbered or bullet points
        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            if (line.matches("^\\d+\\..*") || line.matches("^[-•].*")) {
                recommendations.add(line.replaceAll("^[-•\\d\\.\\s]+", ""));
            }
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Track expenses regularly");
            recommendations.add("Settle debts weekly");
            recommendations.add("Use equal splits for group activities");
        }
        return recommendations;
    }
    
    private Map<String, Object> getFallbackInsights(List<Expense> expenses) {
        Map<String, Object> insights = new HashMap<>();
        insights.put("message", "Using basic analysis (AI service unavailable)");
        insights.put("totalSpent", expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        insights.put("tip", "Try to keep expenses balanced among group members");
        return insights;
    }
    
    private List<String> getQuickTips(BigDecimal userPaid, BigDecimal totalSpent) {
        List<String> tips = new ArrayList<>();
        double percentage = userPaid.doubleValue() / totalSpent.doubleValue() * 100;
        
        if (percentage > 60) {
            tips.add("You're paying more than 60% - ask others to cover next few expenses");
        } else if (percentage < 20) {
            tips.add("You're paying less than 20% - consider contributing more to stay fair");
        } else {
            tips.add("Your contributions are well-balanced - keep it up!");
        }
        
        tips.add("Use the settlement feature to minimize transactions");
        tips.add("Review expenses weekly to avoid surprises");
        
        return tips;
    }
    
    private String categorizeExpense(String description) {
        description = description.toLowerCase();
        if (description.contains("food") || description.contains("restaurant") || 
            description.contains("dinner") || description.contains("lunch"))
            return "Food & Dining";
        if (description.contains("hotel") || description.contains("stay") || 
            description.contains("rent") || description.contains("airbnb"))
            return "Accommodation";
        if (description.contains("taxi") || description.contains("uber") || 
            description.contains("fuel") || description.contains("transport"))
            return "Transportation";
        if (description.contains("movie") || description.contains("party") || 
            description.contains("entertainment") || description.contains("club"))
            return "Entertainment";
        return "Other";
    }
    
    private BigDecimal calculateFairnessScore(Map<String, BigDecimal> userSpending) {
        if (userSpending.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal total = userSpending.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fairShare = total.divide(BigDecimal.valueOf(userSpending.size()), 2, RoundingMode.HALF_UP);
        
        // Calculate variance from fair share
        BigDecimal variance = BigDecimal.ZERO;
        for (BigDecimal amount : userSpending.values()) {
            variance = variance.add(amount.subtract(fairShare).abs());
        }
        
        // Score = 100 - (variance / total * 100)
        BigDecimal score = BigDecimal.valueOf(100)
            .subtract(variance.divide(total, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        
        return score.max(BigDecimal.ZERO);
    }
    
    private String calculateTrend(Map<String, BigDecimal> monthlySpending) {
        if (monthlySpending.size() < 2) return "stable";
        
        List<BigDecimal> values = new ArrayList<>(monthlySpending.values());
        BigDecimal first = values.get(0);
        BigDecimal last = values.get(values.size() - 1);
        
        if (last.compareTo(first) > 0) return "increasing";
        if (last.compareTo(first) < 0) return "decreasing";
        return "stable";
    }
    
    private BigDecimal estimateNextMonth(Map<String, BigDecimal> monthlySpending) {
        if (monthlySpending.isEmpty()) return BigDecimal.ZERO;
        
        // Simple moving average of last 3 months
        List<BigDecimal> values = new ArrayList<>(monthlySpending.values());
        int size = values.size();
        int takeLast = Math.min(3, size);
        
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = size - takeLast; i < size; i++) {
            sum = sum.add(values.get(i));
        }
        
        return sum.divide(BigDecimal.valueOf(takeLast), 2, RoundingMode.HALF_UP);
    }
    
    private String getMostCommonCategory(List<Expense> expenses) {
        Map<String, Long> categoryCount = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> categorizeExpense(e.getDescription()),
                Collectors.counting()
            ));
        
        return categoryCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Other");
    }
    
    private BigDecimal getAverageExpense(List<Expense> expenses) {
        return expenses.stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(expenses.size()), 2, RoundingMode.HALF_UP);
    }
    
    private List<User> getGroupMembers(Long groupId) {
        // Implement based on your Group model
        // For now, return all users who have expenses in this group
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        Set<Long> userIds = new HashSet<>();
        for (Expense expense : expenses) {
            userIds.add(expense.getPaidBy().getId());
        }
        
        return userIds.stream()
            .map(id -> userRepository.findById(id).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    private Map<String, BigDecimal> getSuggestedSplit(List<User> members, BigDecimal totalAmount) {
        Map<String, BigDecimal> split = new HashMap<>();
        BigDecimal perPerson = totalAmount.divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);
        
        for (User member : members) {
            split.put(member.getName(), perPerson);
        }
        
        return split;
    }
}