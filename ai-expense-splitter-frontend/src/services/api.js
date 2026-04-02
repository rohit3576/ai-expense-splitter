import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// User APIs
export const userAPI = {
    getAll: () => api.get('/users'),
    create: (user) => api.post('/users', user),
    getById: (id) => api.get(`/users/${id}`),
};

// Group APIs
export const groupAPI = {
    getAll: () => api.get('/groups'),
    create: (group) => api.post('/groups', group),
    getById: (id) => api.get(`/groups/${id}`),
};

// Expense APIs
export const expenseAPI = {
    create: (expense) => api.post('/expenses', expense),
    getByGroup: (groupId) => api.get(`/expenses/group/${groupId}`),
};

// Settlement APIs
export const settlementAPI = {
    getPlan: (groupId) => api.get(`/settle/${groupId}`),
    getBalances: (groupId) => api.get(`/settle/${groupId}/balances`),
};

// AI APIs
export const aiAPI = {
    getSmartInsights: (groupId) => api.get(`/settle/${groupId}/ai/smart-insights`),
    getRecommendations: (groupId, userId) => api.get(`/settle/${groupId}/ai/recommendations/${userId}`),
    getFairnessAnalysis: (groupId) => api.get(`/settle/${groupId}/ai/fairness-analysis`),
    predictTrends: (groupId) => api.get(`/settle/${groupId}/ai/predict-trends`),
    getSplittingAdvice: (groupId, expense) => api.post(`/settle/${groupId}/ai/splitting-advice`, expense),
};

export default api;