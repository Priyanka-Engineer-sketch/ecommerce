import React from 'react';

interface AdminUserRow {
    id: number;
    username: string;
    email: string;
    roles: string[];
    active: boolean;
    emailVerified: boolean;
    createdAt: string;
}

const mockAdminUsers: AdminUserRow[] = [
    {
        id: 1,
        username: 'admin',
        email: 'admin@ecomm.local',
        roles: ['ROLE_ADMIN', 'ROLE_USER'],
        active: true,
        emailVerified: true,
        createdAt: '2025-05-01 10:22',
    },
    {
        id: 2,
        username: 'seller01',
        email: 'seller01@ecomm.local',
        roles: ['ROLE_SELLER'],
        active: true,
        emailVerified: false,
        createdAt: '2025-05-03 15:10',
    },
    {
        id: 3,
        username: 'user01',
        email: 'user01@ecomm.local',
        roles: ['ROLE_USER'],
        active: false,
        emailVerified: false,
        createdAt: '2025-05-05 09:45',
    },
];

const UserTable: React.FC = () => {
    return (
        <div className="card shadow-sm border-0 mt-4">
            <div className="card-header bg-white d-flex justify-content-between align-items-center">
                <div>
                    <h5 className="mb-0">Platform users</h5>
                    <small className="text-muted">
                        Admins, sellers and customers from your e-commerce backend
                    </small>
                </div>
                <div className="d-flex gap-2">
                    <input
                        className="form-control form-control-sm"
                        placeholder="Search by email or username"
                    />
                    <select className="form-select form-select-sm">
                        <option>All roles</option>
                        <option>Admins</option>
                        <option>Sellers</option>
                        <option>Customers</option>
                    </select>
                </div>
            </div>

            <div className="card-body p-0">
                <table className="table mb-0 align-middle">
                    <thead className="table-light">
                    <tr>
                        <th style={{ width: 40 }}>
                            <input className="form-check-input" type="checkbox" />
                        </th>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Roles</th>
                        <th>Status</th>
                        <th>Email verified</th>
                        <th>Created at</th>
                    </tr>
                    </thead>
                    <tbody>
                    {mockAdminUsers.map((u) => (
                        <tr key={u.id}>
                            <td>
                                <input className="form-check-input" type="checkbox" />
                            </td>
                            <td className="fw-semibold">{u.username}</td>
                            <td>{u.email}</td>
                            <td>
                                {u.roles.map((r) => (
                                    <span key={r} className="badge bg-light text-muted me-1">
                      {r.replace('ROLE_', '')}
                    </span>
                                ))}
                            </td>
                            <td>
                  <span
                      className={
                          'badge rounded-pill ' +
                          (u.active
                              ? 'bg-success-subtle text-success'
                              : 'bg-secondary-subtle text-secondary')
                      }
                  >
                    {u.active ? 'Active' : 'Disabled'}
                  </span>
                            </td>
                            <td>
                                {u.emailVerified ? (
                                    <span className="text-success small">
                      <i className="bi bi-check-circle me-1" />
                      Verified
                    </span>
                                ) : (
                                    <span className="text-muted small">
                      <i className="bi bi-clock-history me-1" />
                      Pending
                    </span>
                                )}
                            </td>
                            <td className="text-muted small">{u.createdAt}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                <div className="px-3 py-2 d-flex justify-content-between small text-muted">
                    <span>1 to {mockAdminUsers.length} of {mockAdminUsers.length} users</span>
                    <span>Previous · Next</span>
                </div>
            </div>
        </div>
    );
};

export default UserTable;
