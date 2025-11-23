// src/pages/users/users.ts

export interface UserSummary {
    id: number;
    name: string;
    email: string;
    role: string;
    active: boolean;
}

export const sampleUsers: UserSummary[] = [
    {
        id: 1,
        name: 'Admin User',
        email: 'admin@example.com',
        role: 'ADMIN',
        active: true,
    },
    {
        id: 2,
        name: 'Test Seller',
        email: 'seller@example.com',
        role: 'SELLER',
        active: true,
    },
];
