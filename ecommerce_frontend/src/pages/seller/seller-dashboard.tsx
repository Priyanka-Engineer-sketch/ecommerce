
import React from 'react';
import Sidebar from '../../components/Sidebar';
import Navbar from '../../components/Navbar';

const SellerDashboard: React.FC = () => {
    return (
        <div className="dashboard-layout">
        <Sidebar />
        <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content">
        <h2 className="fw-semibold">Seller Dashboard</h2>
    <p className="text-muted">
        Seller-specific metrics like product performance, payouts and order
    health. (Placeholder content for now.)
        </p>
        </div>
        </div>
        </div>
);
};

export default SellerDashboard;
