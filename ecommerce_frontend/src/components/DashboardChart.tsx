// src/components/DashboardChart.tsx
import React from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';

interface ChartProps {
    data: any[];
}
const DashboardChart: React.FC<ChartProps> = ({ data }) => (
    <ResponsiveContainer width="100%" height={200}>
        <LineChart data={data}>
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Line type="monotone" dataKey="sales" stroke="#8884d8" />
            <Line type="monotone" dataKey="profit" stroke="#82ca9d" />
        </LineChart>
    </ResponsiveContainer>
);
export default DashboardChart;
