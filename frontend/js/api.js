/**
 * Shoe Store — API Module
 * Quản lý JWT token, user info, và gọi API
 */

const API_BASE = 'http://localhost:8080';

/* ─── Token Management ─── */
function getToken() {
    return localStorage.getItem('shoe_store_token');
}

function setToken(token) {
    localStorage.setItem('shoe_store_token', token);
}

function removeToken() {
    localStorage.removeItem('shoe_store_token');
}

/* ─── User Info Management ─── */
function getUserInfo() {
    const data = localStorage.getItem('shoe_store_user');
    try {
        return data ? JSON.parse(data) : null;
    } catch {
        return null;
    }
}

function setUserInfo(user) {
    localStorage.setItem('shoe_store_user', JSON.stringify(user));
}

function removeUserInfo() {
    localStorage.removeItem('shoe_store_user');
}

/* ─── API Request Wrapper ─── */
async function apiRequest(url, method = 'GET', body = null) {
    const headers = {
        'Content-Type': 'application/json',
    };

    const token = getToken();
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    const options = { method, headers };
    if (body && method !== 'GET') {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(API_BASE + url, options);

    if (!response.ok) {
        let errorData = {};
        try {
            errorData = await response.json();
        } catch { /* ignore parse error */ }

        const error = new Error(errorData.message || 'Có lỗi xảy ra');
        error.status = response.status;
        error.data = errorData;

        // Auto redirect to login on 401
        if (response.status === 401 || response.status === 403) {
            removeToken();
            removeUserInfo();
            if (!window.location.pathname.includes('login.html')) {
                window.location.href = 'login.html';
                return;
            }
        }

        throw error;
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

/* ─── Auth Helpers ─── */
function isLoggedIn() {
    return !!getToken();
}

function logout() {
    removeToken();
    removeUserInfo();
    window.location.href = 'login.html';
}

function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}