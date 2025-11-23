// src/pages/admin/AdminDashboard.tsx
import React from 'react';
import Sidebar from '../../components/Sidebar';
import AppNavbar from '../../components/Navbar';
import KpiCard from '../../components/KpiCard';
import DashboardChart from '../../components/DashboardChart';
import { Container, Row, Col } from 'react-bootstrap';

const data: any[] = [ /* ...your chart data here... */ ];

const AdminDashboard = () => (
    <div className="d-flex">
        <Sidebar />
        <Container fluid>
            <AppNavbar />
            <Row>
                <Col md={3}><KpiCard
                    title="Average Sales"
                    value={50897}
                    delta={8}
                    unit="%"
                    positive
                /></Col>
                <Col md={3}><KpiCard
                    title="Total Sales"
                    value={550897}
                    delta={3.48}
                    unit="%"
                    positive
                />
                </Col>
                {/* More KPI cards */}
            </Row>
            <Row>
                <Col md={8}><DashboardChart data={data} /></Col>
                {/* Additional charts/components */}
            </Row>
        </Container>
    </div>
);

export default AdminDashboard;
