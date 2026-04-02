import React, { useState, useEffect } from 'react';
import { aiAPI } from '../../services/api';

const AIInsights = ({ groupId }) => {
    const [insights, setInsights] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (groupId) {
            loadInsights();
        }
    }, [groupId]);

    const loadInsights = async () => {
        try {
            const response = await aiAPI.getSmartInsights(groupId);
            setInsights(response.data);
        } catch (error) {
            console.error('Error loading insights:', error);
        } finally {
            setLoading(false);
        }
    };

    if (!groupId) {
        return <div className="warning">Please select a group first to see AI insights</div>;
    }

    if (loading) {
        return <div className="loading">Loading AI insights...</div>;
    }

    return (
        <div className="ai-insights">
            <h2>🤖 AI-Powered Insights</h2>
            
            <div className="insight-card">
                <h3>💡 Smart Analysis</h3>
                {insights ? (
                    <div>
                        <p className="insight-text">{insights.aiAnalysis || insights.message || "AI analysis complete"}</p>
                        {insights.recommendations && (
                            <ul className="recommendations-list">
                                {insights.recommendations.map((rec, i) => (
                                    <li key={i}>✓ {rec}</li>
                                ))}
                            </ul>
                        )}
                        {insights.summary && (
                            <div className="summary-stats">
                                <span>Total: ₹{insights.summary.totalExpenses}</span>
                                <span>Avg: ₹{insights.summary.averageExpense}</span>
                                <span>Top Category: {insights.summary.mostCommonCategory}</span>
                            </div>
                        )}
                    </div>
                ) : (
                    <p>No insights available yet. Add more expenses!</p>
                )}
            </div>
        </div>
    );
};

export default AIInsights;