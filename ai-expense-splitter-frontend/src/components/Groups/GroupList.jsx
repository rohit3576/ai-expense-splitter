import React, { useState, useEffect } from 'react';
import { groupAPI } from '../../services/api';

const GroupList = () => {
    const [groups, setGroups] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadGroups();
    }, []);

    const loadGroups = async () => {
        try {
            const response = await groupAPI.getAll();
            setGroups(response.data);
        } catch (error) {
            console.error('Error loading groups:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="loading">Loading groups...</div>;

    return (
        <div className="group-list">
            <div className="card">
                <h2>Groups</h2>
                <button className="btn-primary" onClick={() => window.location.href='/create-group'}>
                    Create New Group
                </button>
                <table className="balances-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Members</th>
                        </tr>
                    </thead>
                    <tbody>
                        {groups.map(group => (
                            <tr key={group.id}>
                                <td>{group.id}</td>
                                <td>{group.name}</td>
                                <td>{group.members?.map(m => m.name).join(', ')}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default GroupList;