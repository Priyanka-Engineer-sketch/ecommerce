// src/App.tsx
import React from 'react';
import './App.css';
import AdminDashboard from "./pages/admin/AdminDashboard";


const App: React.FC = () => {
    return (
        <div className="App">
            <AdminDashboard />
        </div>
    );
};

export default App;
