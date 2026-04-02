import React, { useState, useEffect } from 'react';
import { settlementAPI } from '../../services/api';

const SettlementDashboard = ({ groupId }) => {
    const [settlement, setSettlement] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (groupId) {
            loadSettlement();
        }
    }, [groupId]);

    const loadSettlement = async () => {
        try {
            setLoading(true);
            const response = await settlementAPI.getPlan(groupId);
            setSettlement(response.data);
        } catch (error) {
            console.error('Error loading settlement:', error);
        } finally {
            setLoading(false);
        }
    };

    if (!groupId) {
        return <div className="warning">Please select a group first</div>;
    }

    if (loading) {
        return <div className="loading">Loading settlement plan...</div>;
    }

    if (!settlement) {
        return <div className="warning">No expenses found for this group</div>;
    }

    return (
        <div className="settlement-dashboard">
            <h2>Settlement Plan - {settlement.groupName}</h2>
            
            <div className="summary-card">
                <h3>Summary</h3>
                <p>Total Spent: <strong>₹{settlement.summary.totalSpent}</strong></p>
                <p>Participants: <strong>{settlement.summary.numberOfParticipants}</strong></p>
                <p>Fair Share Per Person: <strong>₹{(settlement.summary.totalSpent / settlement.summary.numberOfParticipants).toFixed(2)}</strong></p>
            </div>

            <div className="transactions-card">
                <h3>Minimized Transactions</h3>
                {settlement.transactions.length === 0 ? (
                    <p className="success">✅ All balances are settled! Everyone is even.</p>
                ) : (
                    <ul className="transaction-list">
                        {settlement.transactions.map((tx, index) => (
                            <li key={index} className="transaction-item">
                                <span className="from">{tx.fromUser}</span>
                                <span className="arrow">→</span>
                                <span className="to">{tx.toUser}</span>
                                <span className="amount">₹{tx.amount}</span>
                            </li>
                        ))}
                    </ul>
                )}
            </div>

            <div className="balances-card">
                <h3>Individual Balances</h3>
                <table className="balances-table">
                    <thead>
                        <tr>
                            <th>User</th>
                            <th>Balance</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {Object.entries(settlement.summary.balances).map(([user, balance]) => (
                            <tr key={user}>
                                <td>{user}</td>
                                <td className={balance >= 0 ? 'positive' : 'negative'}>
                                    ₹{Math.abs(balance)}
                                </td>
                                <td>
                                    {balance > 0 && <span className="badge success">To Receive</span>}
                                    {balance < 0 && <span className="badge danger">To Pay</span>}
                                    {balance === 0 && <span className="badge info">Settled</span>}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default SettlementDashboard;