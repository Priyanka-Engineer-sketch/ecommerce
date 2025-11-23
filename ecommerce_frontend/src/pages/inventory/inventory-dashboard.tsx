import React from 'react';
import Sidebar from '../../components/Sidebar';
import Navbar from '../../components/Navbar';

const InventoryDashboard: React.FC = () => {
    return (
        <div className="dashboard-layout">
        <Sidebar />
        <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content">
        <h2 className="fw-semibold">Inventory Dashboard</h2>
    <p className="text-muted">
        Overview of stock levels, low-inventory alerts and replenishment
    status. (Placeholder – wire to real APIs later.)
    </p>
    </div>
    </div>
    </div>
);
};

export default InventoryDashboard;
