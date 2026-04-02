import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { groupAPI, userAPI } from '../../services/api';

const CreateGroup = () => {
    const navigate = useNavigate();
    const [group, setGroup] = useState({ name: '', members: [] });
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        try {
            const response = await userAPI.getAll();
            setUsers(response.data);
        } catch (error) {
            console.error('Error loading users:', error);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await groupAPI.create(group);
            navigate('/groups');
        } catch (error) {
            console.error('Error creating group:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleMemberToggle = (userId) => {
        const members = group.members.includes(userId)
            ? group.members.filter(id => id !== userId)
            : [...group.members, userId];
        setGroup({ ...group, members });
    };

    return (
        <div className="card">
            <h2>Create New Group</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>Group Name:</label>
                    <input
                        type="text"
                        value={group.name}
                        onChange={(e) => setGroup({ ...group, name: e.target.value })}
                        required
                    />
                </div>
                <div className="form-group">
                    <label>Select Members:</label>
                    {users.map(user => (
                        <div key={user.id}>
                            <label>
                                <input
                                    type="checkbox"
                                    checked={group.members.includes(user.id)}
                                    onChange={() => handleMemberToggle(user.id)}
                                />
                                {user.name} ({user.email})
                            </label>
                        </div>
                    ))}
                </div>
                <button type="submit" className="btn-primary" disabled={loading}>
                    {loading ? 'Creating...' : 'Create Group'}
                </button>
            </form>
        </div>
    );
};

export default CreateGroup;