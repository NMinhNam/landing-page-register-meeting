// Common Admin Functions

// 1. Auth Check on load
const token = localStorage.getItem('adminToken');
if (!token && !window.location.pathname.includes('/login')) {
    window.location.href = '/admin/login';
}

// 2. Logout Function
function logout() {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminId');
    localStorage.removeItem('adminUnitId');
    localStorage.removeItem('adminRole');
    localStorage.removeItem('adminUser');
    window.location.href = '/admin/login';
}

// 3. Helper to get headers
function getHeaders() {
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };
}

// 4. Role Check Helper
function isAdmin() {
    return localStorage.getItem('adminRole') !== 'VIEWER';
}
