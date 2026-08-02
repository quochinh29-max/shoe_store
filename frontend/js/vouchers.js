/**
 * Shoe Store — Vouchers Module
 * CRUD voucher (mã giảm giá)
 */

var editingVoucherId = null;

document.addEventListener('DOMContentLoaded', function () {
    if (!requireAuth()) return;

    loadVouchers();

    document.getElementById('addVoucherBtn').addEventListener('click', function () {
        openModal();
    });

    document.getElementById('voucherForm').addEventListener('submit', handleSaveVoucher);

    document.getElementById('modalClose').addEventListener('click', closeModal);
    document.getElementById('cancelBtn').addEventListener('click', closeModal);
    document.getElementById('voucherModal').addEventListener('click', function (e) {
        if (e.target === this) closeModal();
    });
});

/* ─── Load Vouchers ─── */
async function loadVouchers() {
    var tbody = document.getElementById('vouchersTableBody');
    tbody.innerHTML = '<tr><td colspan="8" class="table-empty"><div class="spinner"></div></td></tr>';

    try {
        var vouchers = await apiRequest('/api/vouchers');
        renderTable(vouchers);
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="8" class="table-empty">' +
            '<div class="table-empty-icon">❌</div>' +
            '<p>Không thể tải danh sách voucher</p>' +
            '<p class="text-muted">' + escapeHtml(error.message) + '</p>' +
            '</td></tr>';
    }
}

/* ─── Render Table ─── */
function renderTable(vouchers) {
    var tbody = document.getElementById('vouchersTableBody');

    if (!vouchers || vouchers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="table-empty">' +
            '<div class="table-empty-icon">🎫</div>' +
            '<p>Chưa có voucher nào</p>' +
            '</td></tr>';
        return;
    }

    var statusMap = {
        ACTIVE:      { label: 'Đang hoạt động', cls: 'status-active' },
        UPCOMING:    { label: 'Sắp diễn ra',     cls: 'status-upcoming' },
        EXPIRED:     { label: 'Đã hết hạn',      cls: 'status-expired' },
        OUT_OF_USES: { label: 'Hết lượt dùng',   cls: 'status-expired' }
    };

    var html = '';
    for (var i = 0; i < vouchers.length; i++) {
        var v = vouchers[i];
        var discountText = v.discountType === 'PERCENT'
            ? v.discountValue + '%'
            : formatCurrency(v.discountValue);
        var st = statusMap[v.status] || { label: v.status, cls: '' };
        var usageText = (v.usageLimit != null)
            ? v.usedCount + ' / ' + v.usageLimit
            : v.usedCount + ' / ∞';

        html += '<tr>' +
            '<td><strong>' + v.id + '</strong></td>' +
            '<td><span class="voucher-code">' + escapeHtml(v.code) + '</span></td>' +
            '<td>' + discountText + '</td>' +
            '<td>' + formatCurrency(v.minOrderValue) + '</td>' +
            '<td>' + formatDateRange(v.startDate, v.endDate) + '</td>' +
            '<td>' + usageText + '</td>' +
            '<td><span class="voucher-status ' + st.cls + '">' + st.label + '</span></td>' +
            '<td><div class="table-actions">' +
            '<button class="btn-icon edit" onclick="openModal(' + v.id + ')" title="Sửa">✏️</button>' +
            '<button class="btn-icon delete" onclick="confirmDelete(' + v.id + ', \'' + escapeHtml(v.code).replace(/'/g, "\\'") + '\')" title="Xóa">🗑️</button>' +
            '</div></td>' +
            '</tr>';
    }

    tbody.innerHTML = html;
}

/* ─── Modal ─── */
function openModal(voucherId) {
    editingVoucherId = voucherId || null;
    var modal = document.getElementById('voucherModal');
    var title = document.getElementById('modalTitle');
    var form = document.getElementById('voucherForm');

    form.reset();

    if (editingVoucherId) {
        title.textContent = '✏️ Sửa voucher';
        loadVoucherForEdit(editingVoucherId);
    } else {
        title.textContent = '➕ Thêm voucher mới';
    }

    modal.classList.add('active');
}

async function loadVoucherForEdit(id) {
    try {
        var v = await apiRequest('/api/vouchers/' + id);

        document.getElementById('voucherCode').value = v.code || '';
        document.getElementById('voucherDiscountType').value = v.discountType || 'PERCENT';
        document.getElementById('voucherDiscountValue').value = v.discountValue != null ? v.discountValue : '';
        document.getElementById('voucherMinOrder').value = v.minOrderValue != null ? v.minOrderValue : '';
        document.getElementById('voucherMaxDiscount').value = v.maxDiscount != null ? v.maxDiscount : '';
        document.getElementById('voucherStartDate').value = toDatetimeLocal(v.startDate);
        document.getElementById('voucherEndDate').value = toDatetimeLocal(v.endDate);
        document.getElementById('voucherUsageLimit').value = v.usageLimit != null ? v.usageLimit : '';
    } catch (error) {
        showToast('Không thể tải thông tin voucher: ' + error.message, 'error');
        closeModal();
    }
}

/* ─── Save Voucher (Create / Update) ─── */
async function handleSaveVoucher(e) {
    e.preventDefault();

    var submitBtn = e.target.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span> Đang lưu...';

    var startDate = document.getElementById('voucherStartDate').value;
    var endDate = document.getElementById('voucherEndDate').value;

    if (startDate && endDate && new Date(startDate) >= new Date(endDate)) {
        showToast('Ngày kết thúc phải sau ngày bắt đầu', 'error');
        submitBtn.disabled = false;
        submitBtn.innerHTML = '💾 Lưu voucher';
        return;
    }

    var maxDiscountVal = document.getElementById('voucherMaxDiscount').value;
    var usageLimitVal = document.getElementById('voucherUsageLimit').value;

    var voucherData = {
        code: document.getElementById('voucherCode').value.trim().toUpperCase(),
        discountType: document.getElementById('voucherDiscountType').value,
        discountValue: parseFloat(document.getElementById('voucherDiscountValue').value),
        minOrderValue: parseFloat(document.getElementById('voucherMinOrder').value) || 0,
        maxDiscount: maxDiscountVal ? parseFloat(maxDiscountVal) : null,
        startDate: startDate,
        endDate: endDate,
        usageLimit: usageLimitVal ? parseInt(usageLimitVal, 10) : null
    };

    try {
        if (editingVoucherId) {
            await apiRequest('/api/vouchers/' + editingVoucherId, 'PUT', voucherData);
            showToast('Cập nhật voucher thành công! ✅', 'success');
        } else {
            await apiRequest('/api/vouchers', 'POST', voucherData);
            showToast('Thêm voucher mới thành công! ✅', 'success');
        }

        closeModal();
        loadVouchers();
    } catch (error) {
        showToast(getErrorMessage(error), 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '💾 Lưu voucher';
    }
}

/* ─── Delete Voucher ─── */
async function confirmDelete(id, code) {
    if (!confirm('Bạn có chắc chắn muốn xóa voucher "' + code + '"?\n\nHành động này không thể hoàn tác.')) {
        return;
    }

    try {
        await apiRequest('/api/vouchers/' + id, 'DELETE');
        showToast('Đã xóa voucher thành công! 🗑️', 'success');
        loadVouchers();
    } catch (error) {
        showToast(getErrorMessage(error), 'error');
    }
}

function closeModal() {
    document.getElementById('voucherModal').classList.remove('active');
    editingVoucherId = null;
}

/* ─── Utility ─── */
function formatCurrency(amount) {
    if (!amount && amount !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' ₫';
}

function formatDateRange(start, end) {
    return formatShortDateTime(start) + ' → ' + formatShortDateTime(end);
}

function formatShortDateTime(isoStr) {
    if (!isoStr) return '—';
    var d = new Date(isoStr);
    if (isNaN(d.getTime())) return '—';
    var day = String(d.getDate()).padStart(2, '0');
    var month = String(d.getMonth() + 1).padStart(2, '0');
    return day + '/' + month + '/' + d.getFullYear();
}

/**
 * Chuyển ISO datetime từ backend sang định dạng phù hợp cho input type="datetime-local"
 * (yyyy-MM-ddTHH:mm)
 */
function toDatetimeLocal(isoStr) {
    if (!isoStr) return '';
    var d = new Date(isoStr);
    if (isNaN(d.getTime())) return '';
    var pad = function (n) { return String(n).padStart(2, '0'); };
    return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
           'T' + pad(d.getHours()) + ':' + pad(d.getMinutes());
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
    toast.innerHTML = message;
    container.appendChild(toast);

    setTimeout(function () {
        toast.remove();
    }, 4000);
}
