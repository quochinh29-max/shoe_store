/**
 * Shoe Store — App Module v2
 * Auth guard, sidebar/header rendering, logout
 */

document.addEventListener('DOMContentLoaded', function () {
    var publicPages = ['/login.html', '/register.html', 'login.html', 'register.html'];
    var currentPath = window.location.pathname;

    var isPublic = publicPages.some(function (page) {
        return currentPath.endsWith(page);
    });

    // Auth guard: redirect to login if not authenticated
    if (!isPublic && !isLoggedIn()) {
        window.location.href = 'login.html';
        return;
    }

    // If already logged in and on auth pages, go to dashboard
    if (isPublic && isLoggedIn()) {
        window.location.href = 'index.html';
        return;
    }

    // Render user info on protected pages
    if (!isPublic) {
        renderUserInfo();
        bindLogout();
    }
});

function renderUserInfo() {
    var user = getUserInfo();
    if (!user) return;

    var displayName = user.fullName || user.username;
    var initials    = displayName.substring(0, 2).toUpperCase();
    var role        = user.role || 'USER';

    // Sidebar
    var sidebarAvatar   = document.getElementById('sidebarAvatar');
    var sidebarUserName = document.getElementById('sidebarUserName');
    var sidebarUserRole = document.getElementById('sidebarUserRole');

    if (sidebarAvatar)   sidebarAvatar.textContent   = initials;
    if (sidebarUserName) sidebarUserName.textContent = displayName;
    if (sidebarUserRole) sidebarUserRole.textContent = role;

    // Header
    var headerAvatar   = document.getElementById('headerAvatar');
    var headerUserName = document.getElementById('headerUserName');
    var headerUserRole = document.getElementById('headerUserRole');

    if (headerAvatar)   headerAvatar.textContent   = initials;
    if (headerUserName) headerUserName.textContent = displayName;
    if (headerUserRole) headerUserRole.textContent = role;

    // Welcome name (dashboard)
    var welcomeName = document.getElementById('welcomeName');
    if (welcomeName) welcomeName.textContent = displayName;
}

function bindLogout() {
    var logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function (e) {
            e.preventDefault();
            logout();
        });
    }
}
