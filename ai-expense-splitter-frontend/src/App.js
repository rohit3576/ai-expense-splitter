import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './App.css';
import UserList from './components/Users/UserList';
import CreateUser from './components/Users/CreateUser';
import GroupList from './components/Groups/GroupList';
import CreateGroup from './components/Groups/CreateGroup';
import ExpenseList from './components/Expenses/ExpenseList';
import AddExpense from './components/Expenses/AddExpense';
import SettlementDashboard from './components/Settlement/SettlementDashboard';
import AIInsights from './components/AIInsights/AIInsights';
import { groupAPI } from './services/api';

function App() {
    const [groups, setGroups] = useState([]);
    const [selectedGroup, setSelectedGroup] = useState(null);

    useEffect(() => {
        loadGroups();
    }, []);

    const loadGroups = async () => {
        try {
            const response = await groupAPI.getAll();
            setGroups(response.data);
        } catch (error) {
            console.error('Error loading groups:', error);
        }
    };

    return (
        <Router>
            <div className="App">
                <nav className="navbar">
                    <div className="nav-container">
                        <h1 className="logo">💰 AI Expense Splitter</h1>
                        <ul className="nav-menu">
                            <li><Link to="/">Home</Link></li>
                            <li><Link to="/users">Users</Link></li>
                            <li><Link to="/groups">Groups</Link></li>
                            {selectedGroup && (
                                <>
                                    <li><Link to="/expenses">Expenses</Link></li>
                                    <li><Link to="/settlement">Settlement</Link></li>
                                    <li><Link to="/ai-insights">AI Insights</Link></li>
                                </>
                            )}
                        </ul>
                        <div className="group-selector">
                            <select 
                                onChange={(e) => {
                                    const group = groups.find(g => g.id === parseInt(e.target.value));
                                    setSelectedGroup(group);
                                }}
                                value={selectedGroup?.id || ''}
                            >
                                <option value="">Select Group</option>
                                {groups.map(group => (
                                    <option key={group.id} value={group.id}>
                                        {group.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                </nav>

                <div className="container">
                    <Routes>
                        <Route path="/" element={<Home selectedGroup={selectedGroup} />} />
                        <Route path="/users" element={<UserList />} />
                        <Route path="/create-user" element={<CreateUser />} />
                        <Route path="/groups" element={<GroupList />} />
                        <Route path="/create-group" element={<CreateGroup />} />
                        <Route path="/expenses" element={<ExpenseList groupId={selectedGroup?.id} />} />
                        <Route path="/add-expense" element={<AddExpense groupId={selectedGroup?.id} />} />
                        <Route path="/settlement" element={<SettlementDashboard groupId={selectedGroup?.id} />} />
                        <Route path="/ai-insights" element={<AIInsights groupId={selectedGroup?.id} />} />
                    </Routes>
                </div>
            </div>
        </Router>
    );
}

const Home = ({ selectedGroup }) => {
    return (
        <div className="home">
            <h2>Welcome to AI Expense Splitter</h2>
            {!selectedGroup ? (
                <div className="card">
                    <p>Please select a group from the dropdown above to get started.</p>
                    <Link to="/groups" className="btn-primary">Create a Group</Link>
                </div>
            ) : (
                <div className="dashboard">
                    <div className="stats-grid">
                        <div className="stat-card">
                            <h3>Active Group</h3>
                            <p className="stat-value">{selectedGroup.name}</p>
                        </div>
                        <div className="stat-card">
                            <h3>Members</h3>
                            <p className="stat-value">{selectedGroup.members?.length || 0}</p>
                        </div>
                        <div className="stat-card">
                            <h3>AI Powered</h3>
                            <p className="stat-value">✓ Active</p>
                        </div>
                    </div>
                    <div className="quick-actions">
                        <Link to="/add-expense" className="btn-primary">Add Expense</Link>
                        <Link to="/settlement" className="btn-secondary">View Settlement</Link>
                        <Link to="/ai-insights" className="btn-secondary">AI Insights</Link>
                    </div>
                </div>
            )}
        </div>
    );
};

export default App;