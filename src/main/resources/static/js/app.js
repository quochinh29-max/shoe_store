/**
 * Shoe Store — App Module
 * Auth guard, navbar rendering, logout
 */

document.addEventListener('DOMContentLoaded', function () {
    const publicPages = ['/login.html', '/register.html', 'login.html', 'register.html'];
    const currentPath = window.location.pathname;
    
    // Auth guard check
    const isPublic = publicPages.some(page => currentPath.endsWith(page));

    // Auth guard: redirect to login if not authenticated
    if (!isPublic && !isLoggedIn()) {
        window.location.href = 'login.html';
        return;
    }

    // If already logged in and visiting auth pages, redirect to dashboard
    if (isPublic && isLoggedIn()) {
        window.location.href = 'index.html';
        return;
    }

    // Render navbar on protected pages
    if (!isPublic) {
        renderNavbar();
    }
});

function renderNavbar() {
    const user = getUserInfo();
    if (!user) return;

    const navUserName = document.getElementById('navUserName');
    const navUserRole = document.getElementById('navUserRole');
    const logoutBtn = document.getElementById('logoutBtn');

    if (navUserName) navUserName.textContent = user.fullName || user.username;
    if (navUserRole) navUserRole.textContent = user.role;

    if (logoutBtn) {
        logoutBtn.addEventListener('click', function (e) {
            e.preventDefault();
            logout();
        });
    }

    // Highlight active nav link
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-links a').forEach(function (link) {
        const href = link.getAttribute('href');
        if (currentPath.endsWith(href) || (currentPath.endsWith('/') && href === 'index.html')) {
            link.classList.add('active');
        }
    });
}
