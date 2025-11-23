
const API_BASE_URL =
    process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8080';

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
    const url = `${API_BASE_URL}${path}`;

    const headers: HeadersInit = {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
    };

    const response = await fetch(url, { ...options, headers });

    if (!response.ok) {
        // You can customize this error handling later
        const message = await response.text();
        throw new Error(
            `API error ${response.status} ${response.statusText}: ${message}`,
        );
    }

    // If there is no body, just return as unknown
    if (response.status === 204) {
        return undefined as unknown as T;
    }

    return (await response.json()) as T;
}

export const apiClient = {
    get: <T>(path: string, options?: RequestInit) =>
        request<T>(path, { ...(options || {}), method: 'GET' }),
    post: <T, B = unknown>(path: string, body: B, options?: RequestInit) =>
        request<T>(path, {
            ...(options || {}),
            method: 'POST',
            body: JSON.stringify(body),
        }),
    put: <T, B = unknown>(path: string, body: B, options?: RequestInit) =>
        request<T>(path, {
            ...(options || {}),
            method: 'PUT',
            body: JSON.stringify(body),
        }),
    del: <T>(path: string, options?: RequestInit) =>
        request<T>(path, { ...(options || {}), method: 'DELETE' }),
};
