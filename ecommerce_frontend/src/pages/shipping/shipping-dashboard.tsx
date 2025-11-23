// src/pages/shipping/shipping-dashboard.ts
import React from 'react';
import Sidebar from '../../components/Sidebar';
import Navbar from '../../components/Navbar';

const ShippingDashboard: React.FC = () => {
    return (
        <div className="dashboard-layout">
        <Sidebar />
        <div className="dashboard-main">
        <Navbar />
        <div className="dashboard-content">
        <h2 className="fw-semibold">Shipping Dashboard</h2>
    <p className="text-muted">
        Track shipments, delivery SLAs and carrier performance here.
    (Placeholder – integrate shipment APIs later.)
    </p>
    </div>
    </div>
    </div>
);
};

export default ShippingDashboard;
