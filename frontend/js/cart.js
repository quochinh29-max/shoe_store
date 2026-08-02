/**
 * Shoe Store — Cart & Checkout Module
 */

var appliedVoucher = null; // VoucherDTO trả về từ /api/vouchers/validate

document.addEventListener('DOMContentLoaded', function () {
    if (!requireAuth()) return;

    renderCart();

    document.getElementById('applyVoucherBtn').addEventListener('click', handleApplyVoucher);
    document.getElementById('placeOrderBtn').addEventListener('click', handlePlaceOrder);
});

/* ─── Render Cart ─── */
function renderCart() {
    var cart = getCart();
    var wrap = document.getElementById('cartItemsWrap');

    if (!cart || cart.length === 0) {
        wrap.innerHTML = '<div class="empty-cart-box">' +
            '<div class="icon">🛒</div>' +
            '<p>Giỏ hàng của bạn đang trống</p>' +
            '<a href="shop.html" class="btn btn-primary" style="margin-top:14px;display:inline-flex;">🛍️ Tiếp tục mua sắm</a>' +
            '</div>';
        document.getElementById('placeOrderBtn').disabled = true;
        renderSummary(0);
        return;
    }

    document.getElementById('placeOrderBtn').disabled = false;

    var html = '';
    cart.forEach(function (item) {
        var thumb = item.imageUrl
            ? '<img class="cart-item-thumb" src="' + escapeHtml(item.imageUrl) + '" onerror="this.style.visibility=\'hidden\'">'
            : '<div class="cart-item-thumb" style="display:flex;align-items:center;justify-content:center;">👟</div>';

        html += '<div class="cart-item-row">' +
            thumb +
            '<div class="cart-item-info">' +
                '<div class="cart-item-name">' + escapeHtml(item.productName) + '</div>' +
                '<div class="cart-item-variant">Size ' + escapeHtml(item.size) + ' · ' + escapeHtml(item.color) + '</div>' +
                '<div class="cart-item-price">' + formatCurrency(item.price) + '</div>' +
                '<span class="cart-item-remove" onclick="handleRemoveItem(' + item.variantId + ')">🗑️ Xóa</span>' +
            '</div>' +
            '<div class="qty-stepper">' +
                '<button type="button" onclick="handleQtyChange(' + item.variantId + ', -1)">−</button>' +
                '<input type="text" value="' + item.quantity + '" readonly>' +
                '<button type="button" onclick="handleQtyChange(' + item.variantId + ', 1)">+</button>' +
            '</div>' +
        '</div>';
    });

    wrap.innerHTML = html;
    renderSummary(getCartSubtotal());
}

function handleQtyChange(variantId, delta) {
    var cart = getCart();
    var item = cart.find(function (c) { return c.variantId === variantId; });
    if (!item) return;

    var next = item.quantity + delta;
    if (next < 1) return;
    if (item.stockQuantity && next > item.stockQuantity) {
        showToast('Chỉ còn ' + item.stockQuantity + ' sản phẩm trong kho', 'error');
        return;
    }

    updateItemQuantity(variantId, next);
    appliedVoucher = null;
    renderCart();
}

function handleRemoveItem(variantId) {
    removeItemFromCart(variantId);
    appliedVoucher = null;
    renderCart();
}

/* ─── Voucher ─── */
async function handleApplyVoucher() {
    var code = document.getElementById('voucherCodeInput').value.trim();
    if (!code) { showToast('Vui lòng nhập mã voucher', 'error'); return; }

    try {
        var voucher = await apiRequest('/api/vouchers/validate?code=' + encodeURIComponent(code));

        if (voucher.status !== 'ACTIVE') {
            var statusText = { UPCOMING: 'chưa bắt đầu', EXPIRED: 'đã hết hạn', OUT_OF_USES: 'đã hết lượt dùng' };
            showToast('Voucher ' + (statusText[voucher.status] || 'không khả dụng'), 'error');
            return;
        }

        var subtotal = getCartSubtotal();
        if (voucher.minOrderValue && subtotal < voucher.minOrderValue) {
            showToast('Đơn hàng cần tối thiểu ' + formatCurrency(voucher.minOrderValue) + ' để dùng mã này', 'error');
            return;
        }

        appliedVoucher = voucher;
        showToast('Áp dụng voucher thành công! 🎉', 'success');
        renderSummary(subtotal);
    } catch (error) {
        showToast('Mã voucher không hợp lệ hoặc không tồn tại', 'error');
    }
}

function removeVoucher() {
    appliedVoucher = null;
    document.getElementById('voucherCodeInput').value = '';
    renderSummary(getCartSubtotal());
}

function computeDiscount(subtotal) {
    if (!appliedVoucher) return 0;

    var discount = appliedVoucher.discountType === 'PERCENT'
        ? subtotal * (appliedVoucher.discountValue / 100)
        : appliedVoucher.discountValue;

    if (appliedVoucher.maxDiscount && discount > appliedVoucher.maxDiscount) {
        discount = appliedVoucher.maxDiscount;
    }
    if (discount > subtotal) discount = subtotal;

    return discount;
}

function renderSummary(subtotal) {
    var discount = computeDiscount(subtotal);
    var total = subtotal - discount;

    document.getElementById('sumSubtotal').textContent = formatCurrency(subtotal);
    document.getElementById('sumDiscount').textContent = '-' + formatCurrency(discount);
    document.getElementById('sumTotal').textContent = formatCurrency(total);

    var appliedWrap = document.getElementById('voucherAppliedWrap');
    if (appliedVoucher) {
        appliedWrap.innerHTML = '<div class="voucher-applied-chip">' +
            '<span>🎫 ' + escapeHtml(appliedVoucher.code) + ' đã áp dụng</span>' +
            '<button type="button" onclick="removeVoucher()">&times;</button>' +
            '</div>';
    } else {
        appliedWrap.innerHTML = '';
    }
}

/* ─── Checkout ─── */
async function handlePlaceOrder() {
    var cart = getCart();
    if (!cart || cart.length === 0) return;

    var shippingAddress = document.getElementById('shippingAddress').value.trim();
    var paymentMethod = document.getElementById('paymentMethod').value;
    var note = document.getElementById('orderNote').value.trim();

    if (!shippingAddress) {
        showToast('Vui lòng nhập địa chỉ giao hàng', 'error');
        return;
    }

    var btn = document.getElementById('placeOrderBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Đang xử lý...';

    var payload = {
        shippingAddress: shippingAddress,
        paymentMethod: paymentMethod,
        note: note || null,
        voucherCode: appliedVoucher ? appliedVoucher.code : null,
        items: cart.map(function (item) {
            return { variantId: item.variantId, quantity: item.quantity };
        })
    };

    try {
        var order = await apiRequest('/api/orders', 'POST', payload);
        clearCart();
        appliedVoucher = null;
        showToast('Đặt hàng thành công! Mã đơn #' + order.id + ' ✅', 'success');
        setTimeout(function () { window.location.href = 'my-orders.html'; }, 1200);
    } catch (error) {
        showToast(getErrorMessage(error), 'error');
        btn.disabled = false;
        btn.innerHTML = '✅ Đặt hàng';
    }
}

/* ─── Utility ─── */
function formatCurrency(amount) {
    if (!amount && amount !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN').format(Math.round(amount)) + ' ₫';
}

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getErrorMessage(error) {
    if (error.data) {
        if (error.data.messages && typeof error.data.messages === 'object') {
            return Object.values(error.data.messages).join(', ');
        }
        if (error.data.message) return error.data.message;
    }
    return error.message || 'Có lỗi xảy ra. Vui lòng thử lại.';
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
