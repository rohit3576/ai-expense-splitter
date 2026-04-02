import React, { useState, useEffect } from 'react';
import { expenseAPI } from '../../services/api';

const ExpenseList = ({ groupId }) => {
    const [expenses, setExpenses] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (groupId) {
            loadExpenses();
        }
    }, [groupId]);

    const loadExpenses = async () => {
        try {
            const response = await expenseAPI.getByGroup(groupId);
            setExpenses(response.data);
        } catch (error) {
            console.error('Error loading expenses:', error);
        } finally {
            setLoading(false);
        }
    };

    if (!groupId) {
        return <div className="warning">Please select a group first</div>;
    }

    if (loading) return <div className="loading">Loading expenses...</div>;

    return (
        <div className="expense-list">
            <div className="card">
                <h2>Expenses</h2>
                <button className="btn-primary" onClick={() => window.location.href='/add-expense'}>
                    Add Expense
                </button>
                {expenses.length === 0 ? (
                    <p>No expenses yet. Add your first expense!</p>
                ) : (
                    <table className="balances-table">
                        <thead>
                            <tr>
                                <th>Description</th>
                                <th>Amount</th>
                                <th>Paid By</th>
                                <th>Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            {expenses.map(expense => (
                                <tr key={expense.id}>
                                    <td>{expense.description}</td>
                                    <td>₹{expense.amount}</td>
                                    <td>{expense.paidBy?.name}</td>
                                    <td>{new Date(expense.date).toLocaleDateString()}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default ExpenseList;