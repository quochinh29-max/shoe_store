/**
 * Shoe Store — App Module v2
 * Auth guard, sidebar/header rendering, logout
 * [MỚI] Điều hướng theo vai trò: ADMIN -> trang quản trị (index.html), USER -> trang mua sắm (shop.html)
 */

document.addEventListener('DOMContentLoaded', function () {
    var publicPages = ['/login.html', '/register.html', 'login.html', 'register.html'];
    // [FIXED] Các trang quản trị — trước đây chỉ cần đăng nhập là vào được (isLoggedIn()),
    // không kiểm tra role, nên 1 tài khoản USER/khách hàng thường gõ thẳng URL (vd: index.html)
    // vẫn xem được toàn bộ trang quản trị. Giờ thêm kiểm tra role === 'ADMIN'.
    var adminPages = ['index.html', 'orders.html', 'customers.html', 'reports.html', 'vouchers.html', 'products.html'];
    var currentPath = window.location.pathname;

    var isPublic = publicPages.some(function (page) {
        return currentPath.endsWith(page);
    });

    // Auth guard: redirect to login if not authenticated
    if (!isPublic && !isLoggedIn()) {
        window.location.href = 'login.html';
        return;
    }

    // Nếu đã đăng nhập mà vào trang login/register -> điều hướng theo vai trò
    if (isPublic && isLoggedIn()) {
        window.location.href = getHomePageForUser();
        return;
    }

    // [FIXED] Role guard: user thường (không phải ADMIN) cố vào trang quản trị -> đưa về trang mua sắm
    if (!isPublic) {
        var isRootPath = currentPath === '/' || currentPath === '' || currentPath.endsWith('/');
        // Lấy tên file cuối cùng trong path rồi so khớp CHÍNH XÁC (không dùng endsWith trên cả path,
        // vì "my-orders.html" cũng kết thúc bằng chuỗi con "orders.html" -> bị nhận nhầm là trang admin)
        var currentFile = currentPath.split('/').pop();
        var isAdminPage = isRootPath || adminPages.indexOf(currentFile) !== -1;
        var currentUser = getUserInfo();
        if (isAdminPage && (!currentUser || currentUser.role !== 'ADMIN')) {
            window.location.href = 'shop.html';
            return;
        }
    }

    // Render user info on protected pages
    if (!isPublic) {
        renderUserInfo();
        bindLogout();
    }
});

/**
 * [MỚI] Trang chủ mặc định theo vai trò của người dùng.
 * ADMIN -> trang quản trị (Dashboard). USER (khách hàng) -> trang mua sắm.
 */
function getHomePageForUser() {
    var user = getUserInfo();
    return (user && user.role === 'ADMIN') ? 'index.html' : 'shop.html';
}

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