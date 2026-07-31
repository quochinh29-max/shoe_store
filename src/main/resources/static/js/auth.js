/**
 * Shoe Store — Auth Module
 * Xử lý đăng nhập và đăng ký
 */

document.addEventListener('DOMContentLoaded', function () {
    var loginForm = document.getElementById('loginForm');
    var registerForm = document.getElementById('registerForm');

    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
});

async function handleLogin(e) {
    e.preventDefault();

    var username = document.getElementById('loginUsername').value.trim();
    var password = document.getElementById('loginPassword').value;
    var alertEl = document.getElementById('loginAlert');
    var submitBtn = e.target.querySelector('button[type="submit"]');

    hideAlert(alertEl);

    if (!username || !password) {
        showAlert(alertEl, 'Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.', 'error');
        return;
    }

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span> Đang đăng nhập...';

    try {
        var response = await apiRequest('/api/auth/login', 'POST', {
            username: username,
            password: password
        });

        // Lưu token và thông tin user
        setToken(response.token);
        setUserInfo({
            username: response.username,
            email: response.email,
            fullName: response.fullName,
            role: response.role
        });

        // Redirect to dashboard
        window.location.href = 'index.html';
    } catch (error) {
        var message = getErrorMessage(error);
        showAlert(alertEl, message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '🔐 Đăng nhập';
    }
}

async function handleRegister(e) {
    e.preventDefault();

    var username = document.getElementById('regUsername').value.trim();
    var email = document.getElementById('regEmail').value.trim();
    var password = document.getElementById('regPassword').value;
    var fullName = document.getElementById('regFullName').value.trim();
    var alertEl = document.getElementById('registerAlert');
    var submitBtn = e.target.querySelector('button[type="submit"]');

    hideAlert(alertEl);

    if (!username || !email || !password || !fullName) {
        showAlert(alertEl, 'Vui lòng nhập đầy đủ thông tin.', 'error');
        return;
    }

    if (password.length < 6) {
        showAlert(alertEl, 'Mật khẩu phải có ít nhất 6 ký tự.', 'error');
        return;
    }

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span> Đang đăng ký...';

    try {
        await apiRequest('/api/auth/register', 'POST', {
            username: username,
            email: email,
            password: password,
            fullName: fullName
        });

        showAlert(alertEl, 'Đăng ký thành công! Đang chuyển sang trang đăng nhập...', 'success');

        setTimeout(function () {
            window.location.href = 'login.html';
        }, 2000);
    } catch (error) {
        var message = getErrorMessage(error);
        showAlert(alertEl, message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '📝 Đăng ký';
    }
}

/* ─── Error Helpers ─── */
function getErrorMessage(error) {
    if (error.data) {
        // Validation errors: { messages: { field: "error" } }
        if (error.data.messages && typeof error.data.messages === 'object') {
            return Object.values(error.data.messages).join('<br>');
        }
        // Single message
        if (error.data.message) {
            return error.data.message;
        }
    }
    return error.message || 'Có lỗi xảy ra. Vui lòng thử lại.';
}

function showAlert(el, message, type) {
    if (!el) return;
    el.innerHTML = message;
    el.className = 'alert alert-' + type + ' visible';
}

function hideAlert(el) {
    if (!el) return;
    el.className = 'alert';
    el.innerHTML = '';
}
