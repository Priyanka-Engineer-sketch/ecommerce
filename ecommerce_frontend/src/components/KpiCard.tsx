// src/components/KpiCard.tsx
import React from 'react';

interface KpiCardProps {
    icon?: string;
    title: string;
    value: string | number;
    subtitle?: string;

    // Added props
    delta?: number;
    unit?: string;
    positive?: boolean;

    pill?: string;
    pillType?: 'success' | 'danger' | 'warning';
}

const KpiCard: React.FC<KpiCardProps> = ({
                                             icon,
                                             title,
                                             value,
                                             subtitle,

                                             delta,
                                             unit,
                                             positive = true,

                                             pill,
                                             pillType = 'success',
                                         }) => {
    const pillClass =
        pillType === 'success'
            ? 'bg-success-subtle text-success'
            : pillType === 'danger'
                ? 'bg-danger-subtle text-danger'
                : 'bg-warning-subtle text-warning';

    const deltaColor = positive ? 'text-success' : 'text-danger';
    const deltaIcon = positive ? 'bi-arrow-up-right' : 'bi-arrow-down-right';

    return (
        <div className="card h-100 shadow-sm border-0">
            <div className="card-body d-flex align-items-center">

                {/* Icon circle */}
                {icon && (
                    <div
                        className="me-3 rounded-circle bg-primary-subtle text-primary d-flex align-items-center justify-content-center"
                        style={{ width: 44, height: 44 }}
                    >
                        <i className={`bi ${icon}`} />
                    </div>
                )}

                {/* Text content */}
                <div className="flex-grow-1">
                    <div className="small text-muted">{title}</div>

                    {/* Value + delta */}
                    <div className="d-flex align-items-baseline gap-2">
                        <div className="fs-4 fw-semibold">{value}</div>

                        {typeof delta === 'number' && (
                            <div className={`small fw-semibold ${deltaColor}`}>
                                <i className={`bi ${deltaIcon} me-1`} />
                                {delta}
                                {unit && <span className="ms-1">{unit}</span>}
                            </div>
                        )}
                    </div>

                    {subtitle && <div className="small text-muted">{subtitle}</div>}
                </div>

                {/* Optional right-side pill */}
                {pill && (
                    <span className={`badge rounded-pill ${pillClass} ms-2`}>
            {pill}
          </span>
                )}
            </div>
        </div>
    );
};

export default KpiCard;
