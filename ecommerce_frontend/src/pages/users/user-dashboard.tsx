// src/pages/users/user-dashboard.ts
import React from 'react';
import Sidebar from '../../components/Sidebar';
import Navbar from '../../components/Navbar';
import UserTable from '../../components/UserTable';

const UserDashboard: React.FC = () => {
    return (
        <div className="dashboard-layout">
        <Sidebar />
        <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content">
        <h2 className="fw-semibold">Users Dashboard</h2>
    <p className="text-muted">
        Manage platform users, roles and account status.
    </p>

    <UserTable />
    </div>
    </div>
    </div>
);
};

export default UserDashboard;
