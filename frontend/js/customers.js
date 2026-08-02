/**
 * Shoe Store — Customers Module
 * Danh sách khách hàng + xem chi tiết lịch sử mua hàng
 */

var allCustomers = [];

document.addEventListener('DOMContentLoaded', function () {
    if (!requireAuth()) return;

    loadCustomers();

    document.getElementById('searchCustomer').addEventListener('input', function () {
        renderTable(filterCustomers(this.value));
    });

    document.getElementById('modalClose').addEventListener('click', closeModal);
    document.getElementById('closeDetailBtn').addEventListener('click', closeModal);
    document.getElementById('customerModal').addEventListener('click', function (e) {
        if (e.target === this) closeModal();
    });
});

/* ─── Load Customers ─── */
async function loadCustomers() {
    var tbody = document.getElementById('customersTableBody');
    tbody.innerHTML = '<tr><td colspan="8" class="table-empty"><div class="spinner"></div></td></tr>';

    try {
        allCustomers = await apiRequest('/api/customers');
        renderTable(allCustomers);
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="8" class="table-empty">' +
            '<div class="table-empty-icon">❌</div>' +
            '<p>Không thể tải danh sách khách hàng</p>' +
            '<p class="text-muted">' + escapeHtml(error.message) + '</p>' +
            '</td></tr>';
    }
}

function filterCustomers(keyword) {
    if (!keyword) return allCustomers;
    var lower = keyword.toLowerCase();
    return allCustomers.filter(function (c) {
        return (c.fullName || '').toLowerCase().includes(lower) ||
               (c.email || '').toLowerCase().includes(lower) ||
               (c.username || '').toLowerCase().includes(lower);
    });
}

/* ─── Render Table ─── */
function renderTable(customers) {
    var tbody = document.getElementById('customersTableBody');

    if (!customers || customers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="table-empty">' +
            '<div class="table-empty-icon">👥</div>' +
            '<p>Không tìm thấy khách hàng nào</p>' +
            '</td></tr>';
        return;
    }

    var html = '';
    for (var i = 0; i < customers.length; i++) {
        var c = customers[i];
        var displayName = c.fullName || c.username;

        html += '<tr>' +
            '<td><strong>' + c.id + '</strong></td>' +
            '<td>' +
                '<div style="display:flex;align-items:center;gap:8px;">' +
                '<div class="customer-avatar-sm">' + getInitials(displayName) + '</div>' +
                '<span style="font-weight:600;">' + escapeHtml(displayName) + '</span>' +
                '</div>' +
            '</td>' +
            '<td>' + escapeHtml(c.email) + '</td>' +
            '<td>' + escapeHtml(c.phoneNumber || '—') + '</td>' +
            '<td>' + c.totalOrders + '</td>' +
            '<td>' + formatCurrency(c.totalSpent) + '</td>' +
            '<td>' + formatShortDate(c.createdAt) + '</td>' +
            '<td><button class="btn-icon edit" onclick="openCustomerDetail(' + c.id + ')" title="Xem chi tiết">👁️</button></td>' +
            '</tr>';
    }

    tbody.innerHTML = html;
}

/* ─── Customer Detail ─── */
async function openCustomerDetail(id) {
    var modal = document.getElementById('customerModal');
    modal.classList.add('active');

    document.getElementById('custDetailName').textContent = 'Đang tải...';
    document.getElementById('custOrdersBody').innerHTML =
        '<tr><td colspan="4" class="table-empty"><div class="spinner dark"></div></td></tr>';

    try {
        var customer = await apiRequest('/api/customers/' + id);
        var displayName = customer.fullName || customer.username;

        document.getElementById('custDetailAvatar').textContent = getInitials(displayName);
        document.getElementById('custDetailName').textContent = displayName;
        document.getElementById('custDetailEmail').textContent = customer.email;
        document.getElementById('custDetailOrders').textContent = customer.totalOrders;
        document.getElementById('custDetailSpent').textContent = formatCurrency(customer.totalSpent);
        document.getElementById('custDetailPhone').textContent = customer.phoneNumber || '—';
        document.getElementById('custDetailJoined').textContent = formatShortDate(customer.createdAt);

        var orders = await apiRequest('/api/customers/' + id + '/orders');
        renderOrderHistory(orders);
    } catch (error) {
        showToast('Không thể tải chi tiết khách hàng: ' + error.message, 'error');
    }
}

function renderOrderHistory(orders) {
    var tbody = document.getElementById('custOrdersBody');

    if (!orders || orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="table-empty"><p>Chưa có đơn hàng nào</p></td></tr>';
        return;
    }

    var statusLabels = {
        PENDING: 'Chờ xử lý', CONFIRMED: 'Đã xác nhận', SHIPPING: 'Đang giao',
        COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy'
    };

    var html = '';
    orders.forEach(function (o) {
        html += '<tr>' +
            '<td><strong>#' + o.id + '</strong></td>' +
            '<td>' + formatShortDate(o.createdAt) + '</td>' +
            '<td>' + formatCurrency(o.finalAmount) + '</td>' +
            '<td><span class="order-status-badge status-' + o.orderStatus + '">' +
                (statusLabels[o.orderStatus] || o.orderStatus) + '</span></td>' +
            '</tr>';
    });

    tbody.innerHTML = html;
}

function closeModal() {
    document.getElementById('customerModal').classList.remove('active');
}

/* ─── Utility ─── */
function getInitials(name) {
    if (!name) return '?';
    return name.trim().substring(0, 2).toUpperCase();
}

function formatCurrency(amount) {
    if (!amount && amount !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' ₫';
}

function formatShortDate(isoStr) {
    if (!isoStr) return '—';
    var d = new Date(isoStr);
    if (isNaN(d.getTime())) return '—';
    var day = String(d.getDate()).padStart(2, '0');
    var month = String(d.getMonth() + 1).padStart(2, '0');
    return day + '/' + month + '/' + d.getFullYear();
}

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showToast(message, type) {
    var container = document.getElementById('toastContainer');
    if (!container) { alert(message); return; }

    var toast = document.createElement('div');
    toast.className = 'toast toast-' + (type || 'info');
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(function () {
        toast.remove();
    }, 4000);
}
