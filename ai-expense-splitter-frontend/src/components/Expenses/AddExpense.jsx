import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { expenseAPI, groupAPI, userAPI } from '../../services/api';

const AddExpense = ({ groupId }) => {
    const navigate = useNavigate();
    const [expense, setExpense] = useState({
        description: '',
        amount: '',
        paidBy: '',
        splits: []
    });
    const [group, setGroup] = useState(null);
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (groupId) {
            loadGroupAndUsers();
        }
    }, [groupId]);

    const loadGroupAndUsers = async () => {
        try {
            const groupResponse = await groupAPI.getById(groupId);
            setGroup(groupResponse.data);
            const usersResponse = await userAPI.getAll();
            setUsers(usersResponse.data);
            
            // Initialize splits for all users
            const initialSplits = usersResponse.data.map(user => ({
                user: { id: user.id },
                amount: ''
            }));
            setExpense(prev => ({ ...prev, splits: initialSplits }));
        } catch (error) {
            console.error('Error loading data:', error);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const expenseData = {
                ...expense,
                amount: parseFloat(expense.amount),
                paidBy: { id: parseInt(expense.paidBy) },
                group: { id: groupId },
                splits: expense.splits.map(split => ({
                    ...split,
                    amount: parseFloat(split.amount)
                }))
            };
            await expenseAPI.create(expenseData);
            navigate('/expenses');
        } catch (error) {
            console.error('Error creating expense:', error);
        } finally {
            setLoading(false);
        }
    };

    const updateSplit = (userId, amount) => {
        const splits = expense.splits.map(split =>
            split.user.id === userId ? { ...split, amount } : split
        );
        setExpense({ ...expense, splits });
    };

    if (!groupId) {
        return <div className="warning">Please select a group first</div>;
    }

    return (
        <div className="card">
            <h2>Add Expense to {group?.name}</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>Description:</label>
                    <input
                        type="text"
                        value={expense.description}
                        onChange={(e) => setExpense({ ...expense, description: e.target.value })}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Amount (₹):</label>
                    <input
                        type="number"
                        value={expense.amount}
                        onChange={(e) => setExpense({ ...expense, amount: e.target.value })}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Paid By:</label>
                    <select
                        value={expense.paidBy}
                        onChange={(e) => setExpense({ ...expense, paidBy: e.target.value })}
                        required
                    >
                        <option value="">Select user</option>
                        {users.map(user => (
                            <option key={user.id} value={user.id}>{user.name}</option>
                        ))}
                    </select>
                </div>
                <div className="form-group">
                    <label>Split Details:</label>
                    {expense.splits.map(split => {
                        const user = users.find(u => u.id === split.user.id);
                        return user ? (
                            <div key={user.id}>
                                <label>
                                    {user.name}:
                                    <input
                                        type="number"
                                        value={split.amount}
                                        onChange={(e) => updateSplit(user.id, e.target.value)}
                                        required
                                    />
                                </label>
                            </div>
                        ) : null;
                    })}
                </div>
                <button type="submit" className="btn-primary" disabled={loading}>
                    {loading ? 'Adding...' : 'Add Expense'}
                </button>
            </form>
        </div>
    );
};

export default AddExpense;