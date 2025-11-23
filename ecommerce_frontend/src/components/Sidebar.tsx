import React from 'react';
import '../App.css';

const Sidebar: React.FC = () => {
    return (
        <div className="sidebar d-flex flex-column">
            {/* Logo */}
            <div className="sidebar-logo mb-4 px-3 pt-3">
                <span className="fw-bold fs-4 text-primary">ecomm</span>
            </div>

            {/* E-commerce */}
            <div className="px-3">
                <div className="sidebar-section-title text-uppercase small text-muted mb-2">
                    E commerce
                </div>
                <ul className="list-unstyled sidebar-menu">
                    <li className="active">Dashboard</li>
                </ul>
            </div>

            {/* Admin menu (matches screenshot items) */}
            <div className="px-3 mt-4">
                <div className="sidebar-section-title text-uppercase small text-muted mb-2">
                    Admin
                </div>
                <ul className="list-unstyled sidebar-menu">
                    <li>Add product</li>
                    <li>Products</li>
                    <li>Customers</li>
                    <li>Customer details</li>
                    <li>Orders</li>
                    <li>Order details</li>
                    <li>Refund</li>
                </ul>
            </div>

            {/* Customer menu */}
            <div className="px-3 mt-4">
                <div className="sidebar-section-title text-uppercase small text-muted mb-2">
                    Customer
                </div>
                <ul className="list-unstyled sidebar-menu">
                    <li>Homepage</li>
                    <li>Product details</li>
                    <li>Products filter</li>
                    <li>Cart</li>
                    <li>Checkout</li>
                    <li>Shipping info</li>
                    <li>Profile</li>
                    <li>Favourite stores</li>
                    <li>Wishlist</li>
                    <li>Order tracking</li>
                    <li>Invoice</li>
                </ul>
            </div>

            <div className="mt-auto p-3 small text-muted">
                <span className="me-2">▸</span>Collapsed view
            </div>
        </div>
    );
};

export default Sidebar;
