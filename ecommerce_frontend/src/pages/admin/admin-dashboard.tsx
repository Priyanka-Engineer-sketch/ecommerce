// src/components/KpiCard.tsx
import React from 'react';


export interface KpiCardProps {
    icon?: string;
    title: string;
    value: string | number;
    subtitle?: string;
    pill?: string;
    pillType?: 'success' | 'danger' | 'warning';
}

const KpiCard: React.FC<KpiCardProps> = ({
                                             icon,
                                             title,
                                             value,
                                             subtitle,
                                             pill,
                                             pillType = 'success',
                                         }) => {
    const pillClass =
        pillType === 'success'
            ? 'bg-success-subtle text-success'
            : pillType === 'danger'
                ? 'bg-danger-subtle text-danger'
                : 'bg-warning-subtle text-warning';

    return (
        <div className="card h-100 shadow-sm border-0">
            <div className="card-body d-flex align-items-center">
                {icon && (
                    <div
                        className="me-3 rounded-circle bg-primary-subtle text-primary d-flex align-items-center justify-content-center"
                        style={{ width: 44, height: 44 }}
                    >
                        <i className={`bi ${icon}`} />
                    </div>
                )}

                <div className="flex-grow-1">
                    <div className="small text-muted">{title}</div>
                    <div className="fs-4 fw-semibold">{value}</div>
                    {subtitle && <div className="small text-muted">{subtitle}</div>}
                </div>

                {pill && (
                    <span className={`badge rounded-pill ${pillClass} ms-2`}>{pill}</span>
                )}
            </div>
        </div>
    );
};

export default KpiCard;
