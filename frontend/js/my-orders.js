/**
 * Shoe Store — My Orders Module (khách hàng)
 */

var myOrders = [];
var currentMyOrderId = null;

var STATUS_LABELS = {
    PENDING: 'Chờ xử lý', CONFIRMED: 'Đã xác nhận', SHIPPING: 'Đang giao',
    COMPLETED: 'Hoàn thành', CANCELLED: 'Đã hủy'
};

var PAYMENT_STATUS_LABELS = {
    PENDING: 'Chưa thanh toán', PAID: 'Đã thanh toán', FAILED: 'Thất bại'
};

document.addEventListener('DOMContentLoaded', function () {
    if (!requireAuth()) return;

    loadMyOrders();

    document.getElementById('modalClose').addEventListener('click', closeModal);
    document.getElementById('closeDetailBtn').addEventListener('click', closeModal);
    document.getElementById('orderModal').addEventListener('click', function (e) {
        if (e.target === this) closeModal();
    });
});

async function loadMyOrders() {
    var tbody = document.getElementById('myOrdersTableBody');
    tbody.innerHTML = '<tr><td colspan="6" class="table-empty"><div class="spinner"></div></td></tr>';

    try {
        myOrders = await apiRequest('/api/orders/my');
        renderTable(myOrders);
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="6" class="table-empty">' +
            '<div class="table-empty-icon">❌</div>' +
            '<p>Không thể tải danh sách đơn hàng</p>' +
            '</td></tr>';
    }
}

function renderTable(orders) {
    var tbody = document.getElementById('myOrdersTableBody');

    if (!orders || orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="table-empty">' +
            '<div class="table-empty-icon">🧾</div>' +
            '<p>Bạn chưa có đơn hàng nào</p>' +
            '<a href="shop.html" class="btn btn-primary" style="margin-top:12px;display:inline-flex;">🛍️ Mua sắm ngay</a>' +
            '</td></tr>';
        return;
    }

    var html = '';
    orders.forEach(function (o) {
        html += '<tr>' +
            '<td><strong>#' + o.id + '</strong></td>' +
            '<td>' + formatShortDate(o.createdAt) + '</td>' +
            '<td>' + formatCurrency(o.finalAmount) + '</td>' +
            '<td>' + (PAYMENT_STATUS_LABELS[o.paymentStatus] || o.paymentStatus) + '</td>' +
            '<td><span class="order-status-badge status-' + o.orderStatus + '">' +
                (STATUS_LABELS[o.orderStatus] || o.orderStatus) + '</span></td>' +
            '<td><button class="btn-icon edit" onclick="openOrderDetail(' + o.id + ')" title="Xem chi tiết">👁️</button></td>' +
            '</tr>';
    });

    tbody.innerHTML = html;
}

async function openOrderDetail(id) {
    currentMyOrderId = id;
    var modal = document.getElementById('orderModal');
    var body = document.getElementById('orderModalBody');
    modal.classList.add('active');
    body.innerHTML = '<div class="table-empty"><div class="spinner dark"></div></div>';

    try {
        var order = await apiRequest('/api/orders/' + id);
        renderOrderDetail(order);
    } catch (error) {
        body.innerHTML = '<div class="table-empty"><p>Không thể tải chi tiết đơn hàng</p></div>';
    }
}

function renderOrderDetail(order) {
    document.getElementById('orderModalTitle').textContent = '🧾 Đơn hàng #' + order.id;

    var itemsHtml = '';
    if (order.items && order.items.length > 0) {
        order.items.forEach(function (item) {
            itemsHtml += '<tr>' +
                '<td>' + escapeHtml(item.productName) + '</td>' +
                '<td>' + escapeHtml(item.size) + '</td>' +
                '<td>' + escapeHtml(item.color) + '</td>' +
                '<td>' + item.quantity + '</td>' +
                '<td>' + formatCurrency(item.unitPrice) + '</td>' +
                '<td>' + formatCurrency(item.subtotal) + '</td>' +
                '</tr>';
        });
    } else {
        itemsHtml = '<tr><td colspan="6" class="table-empty"><p>Không có sản phẩm</p></td></tr>';
    }

    var body = document.getElementById('orderModalBody');
    body.innerHTML =
        '<div class="order-detail-section">' +
            '<div class="order-info-grid">' +
                '<div class="order-info-item"><span>Ngày đặt</span><strong>' + formatFullDate(order.createdAt) + '</strong></div>' +
                '<div class="order-info-item"><span>Phương thức thanh toán</span><strong>' + escapeHtml(order.paymentMethod || '—') + '</strong></div>' +
                '<div class="order-info-item"><span>Trạng thái thanh toán</span><strong>' + (PAYMENT_STATUS_LABELS[order.paymentStatus] || order.paymentStatus) + '</strong></div>' +
                '<div class="order-info-item"><span>Mã giảm giá</span><strong>' + (order.voucherCode ? escapeHtml(order.voucherCode) : '—') + '</strong></div>' +
            '</div>' +
        '</div>' +

        '<div class="order-detail-section">' +
            '<div class="order-detail-label">Địa chỉ giao hàng</div>' +
            '<p style="font-size:13px;color:var(--text-secondary);">' + escapeHtml(order.shippingAddress || '—') + '</p>' +
            (order.note ? '<div class="order-detail-label" style="margin-top:10px;">Ghi chú</div><p style="font-size:13px;color:var(--text-secondary);">' + escapeHtml(order.note) + '</p>' : '') +
        '</div>' +

        '<div class="order-detail-section">' +
            '<div class="order-detail-label">Sản phẩm trong đơn</div>' +
            '<div class="table-container" style="box-shadow:none; border:1px solid var(--border);">' +
                '<table class="data-table">' +
                    '<thead><tr><th>Sản phẩm</th><th>Size</th><th>Màu</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead>' +
                    '<tbody>' + itemsHtml + '</tbody>' +
                '</table>' +
            '</div>' +
        '</div>' +

        '<div class="order-detail-section">' +
            '<div class="order-info-grid">' +
                '<div class="order-info-item"><span>Tạm tính</span><strong>' + formatCurrency(order.totalAmount) + '</strong></div>' +
                '<div class="order-info-item"><span>Giảm giá</span><strong>-' + formatCurrency(order.discountAmount) + '</strong></div>' +
            '</div>' +
            '<div style="margin-top:10px;padding-top:10px;border-top:1px solid var(--border);display:flex;justify-content:space-between;align-items:center;">' +
                '<span style="font-weight:700;font-size:15px;">Tổng cộng</span>' +
                '<span style="font-weight:700;font-size:18px;color:var(--primary);">' + formatCurrency(order.finalAmount) + '</span>' +
            '</div>' +
        '</div>';

    renderFooterActions(order.orderStatus);
}

function renderFooterActions(status) {
    var footer = document.getElementById('orderModalFooter');
    var actionsHtml = '';

    if (status === 'PENDING') {
        actionsHtml = '<button type="button" class="btn btn-danger-soft" onclick="handleCancelOrder()">❌ Hủy đơn hàng</button>';
    }

    footer.innerHTML =
        '<button type="button" class="btn btn-secondary" onclick="closeModal()">Đóng</button>' +
        actionsHtml;
}

async function handleCancelOrder() {
    if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) return;

    try {
        await apiRequest('/api/orders/' + currentMyOrderId + '/status', 'PATCH', { orderStatus: 'CANCELLED' });
        showToast('Đã hủy đơn hàng', 'success');
        await openOrderDetail(currentMyOrderId);
        loadMyOrders();
    } catch (error) {
        showToast(getErrorMessage(error), 'error');
    }
}

function closeModal() {
    document.getElementById('orderModal').classList.remove('active');
    currentMyOrderId = null;
}

/* ─── Utility ─── */
function formatCurrency(amount) {
    if (!amount && amount !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' ₫';
}

function formatShortDate(isoStr) {
    if (!isoStr) return '—';
    var d = new Date(isoStr);
    if (isNaN(d.getTime())) return '—';
    var pad = function (n) { return String(n).padStart(2, '0'); };
    return pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear();
}

function formatFullDate(isoStr) {
    if (!isoStr) return '—';
    var d = new Date(isoStr);
    if (isNaN(d.getTime())) return '—';
    var pad = function (n) { return String(n).padStart(2, '0'); };
    return pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear() +
           ' lúc ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
}

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getErrorMessage(error) {
    if (error.data && error.data.message) return error.data.message;
    return error.message || 'Có lỗi xảy ra.';
}

function showToast(message, type) {
    var container = document.getElementById('toastContainer');
    if (!container) { alert(message); return; }

    var toast = document.createElement('div');
    toast.className = 'toast toast-' + (type || 'info');
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(function () { toast.remove(); }, 3500);
}
