/**
 * Shoe Store — Shop Module
 * Hiển thị sản phẩm, cho khách chọn size/màu và thêm vào giỏ hàng
 */

var allShopProducts = [];
var currentModalProduct = null;
var currentModalVariants = [];
var selectedVariant = null;

document.addEventListener('DOMContentLoaded', function () {
    if (!requireAuth()) return;

    loadShopProducts();

    document.getElementById('searchKeyword').addEventListener('keypress', function (e) {
        if (e.key === 'Enter') handleSearch();
    });

    document.getElementById('variantModalClose').addEventListener('click', closeVariantModal);
    document.getElementById('variantModalCancel').addEventListener('click', closeVariantModal);
    document.getElementById('variantModal').addEventListener('click', function (e) {
        if (e.target === this) closeVariantModal();
    });

    document.getElementById('qtyMinus').addEventListener('click', function () { changeQty(-1); });
    document.getElementById('qtyPlus').addEventListener('click', function () { changeQty(1); });

    document.getElementById('addToCartBtn').addEventListener('click', handleAddToCart);
});

/* ─── Load & Render Products ─── */
async function loadShopProducts() {
    var grid = document.getElementById('productGrid');
    grid.innerHTML = '<div class="table-empty" style="grid-column:1/-1;"><div class="spinner dark"></div></div>';

    try {
        allShopProducts = await apiRequest('/api/products');
        renderProductGrid(allShopProducts);
    } catch (error) {
        grid.innerHTML = '<div class="table-empty" style="grid-column:1/-1;">' +
            '<div class="table-empty-icon">❌</div>' +
            '<p>Không thể tải sản phẩm</p>' +
            '</div>';
    }
}

async function handleSearch() {
    var keyword = document.getElementById('searchKeyword').value.trim();
    if (!keyword) { renderProductGrid(allShopProducts); return; }

    var grid = document.getElementById('productGrid');
    grid.innerHTML = '<div class="table-empty" style="grid-column:1/-1;"><div class="spinner dark"></div></div>';

    try {
        var products = await apiRequest('/api/products/search?keyword=' + encodeURIComponent(keyword));
        renderProductGrid(products);
    } catch (error) {
        showToast('Lỗi tìm kiếm: ' + error.message, 'error');
    }
}

function renderProductGrid(products) {
    var grid = document.getElementById('productGrid');

    if (!products || products.length === 0) {
        grid.innerHTML = '<div class="table-empty" style="grid-column:1/-1;">' +
            '<div class="table-empty-icon">👟</div>' +
            '<p>Không tìm thấy sản phẩm nào</p>' +
            '</div>';
        return;
    }

    var html = '';
    products.forEach(function (p) {
        var thumb = p.imageUrl
            ? '<img src="' + escapeHtml(p.imageUrl) + '" alt="' + escapeHtml(p.name) + '" onerror="this.parentElement.innerHTML=\'<span class=\\\'shop-product-thumb-placeholder\\\'>👟</span>\'">'
            : '<span class="shop-product-thumb-placeholder">👟</span>';

        var stockText = p.quantity === 0 ? 'Hết hàng' : ('Còn ' + p.quantity + ' sản phẩm');

        html += '<div class="shop-product-card" onclick="openVariantModal(' + p.id + ')">' +
            '<div class="shop-product-thumb-wrap">' + thumb + '</div>' +
            '<div class="shop-product-info">' +
            '<div class="shop-product-brand">' + escapeHtml(p.brand || 'Shoe Store') + '</div>' +
            '<div class="shop-product-name">' + escapeHtml(p.name) + '</div>' +
            '<div class="shop-product-price">' + formatCurrency(p.price) + '</div>' +
            '<div class="shop-product-stock">' + stockText + '</div>' +
            '</div>' +
            '</div>';
    });

    grid.innerHTML = html;
}

/* ─── Variant Modal ─── */
async function openVariantModal(productId) {
    var product = allShopProducts.find(function (p) { return p.id === productId; });
    currentModalProduct = product || await apiRequest('/api/products/' + productId);

    document.getElementById('variantModalName').textContent = currentModalProduct.name;
    document.getElementById('variantModalPrice').textContent = formatCurrency(currentModalProduct.price);

    var thumbWrap = document.getElementById('variantModalThumbWrap');
    thumbWrap.innerHTML = currentModalProduct.imageUrl
        ? '<img src="' + escapeHtml(currentModalProduct.imageUrl) + '" style="width:100%;height:100%;object-fit:cover;" onerror="this.remove()">'
        : '<span style="font-size:28px;opacity:.4;">👟</span>';

    document.getElementById('qtyInput').value = 1;
    selectedVariant = null;

    var modal = document.getElementById('variantModal');
    modal.classList.add('active');

    var optionsWrap = document.getElementById('variantOptions');
    optionsWrap.innerHTML = '<div class="spinner dark"></div>';

    try {
        currentModalVariants = await apiRequest('/api/products/' + productId + '/variants');
        renderVariantOptions();
    } catch (error) {
        optionsWrap.innerHTML = '<p class="text-muted">Không thể tải biến thể sản phẩm</p>';
    }
}

function renderVariantOptions() {
    var wrap = document.getElementById('variantOptions');

    if (!currentModalVariants || currentModalVariants.length === 0) {
        wrap.innerHTML = '<p class="text-muted">Sản phẩm chưa có biến thể size/màu</p>';
        return;
    }

    var html = '';
    currentModalVariants.forEach(function (v) {
        var disabled = v.stockQuantity <= 0;
        html += '<button type="button" class="variant-chip' + (disabled ? ' disabled' : '') + '" ' +
            'data-variant-id="' + v.id + '" ' + (disabled ? 'disabled' : '') + ' onclick="selectVariant(' + v.id + ')">' +
            escapeHtml(v.size) + ' · ' + escapeHtml(v.color) +
            (disabled ? ' (hết hàng)' : '') +
            '</button>';
    });

    wrap.innerHTML = html;
}

function selectVariant(variantId) {
    selectedVariant = currentModalVariants.find(function (v) { return v.id === variantId; });

    document.querySelectorAll('.variant-chip').forEach(function (chip) {
        chip.classList.toggle('selected', parseInt(chip.getAttribute('data-variant-id'), 10) === variantId);
    });

    document.getElementById('qtyInput').value = 1;
    document.getElementById('stockHint').textContent = selectedVariant
        ? ('Còn ' + selectedVariant.stockQuantity + ' trong kho')
        : '';
}

function changeQty(delta) {
    var input = document.getElementById('qtyInput');
    var current = parseInt(input.value, 10) || 1;
    var max = selectedVariant ? selectedVariant.stockQuantity : 99;
    var next = Math.min(Math.max(current + delta, 1), Math.max(max, 1));
    input.value = next;
}

function handleAddToCart() {
    if (!selectedVariant) {
        showToast('Vui lòng chọn size/màu', 'error');
        return;
    }

    var quantity = parseInt(document.getElementById('qtyInput').value, 10) || 1;

    addItemToCart({
        variantId: selectedVariant.id,
        productId: currentModalProduct.id,
        productName: currentModalProduct.name,
        imageUrl: currentModalProduct.imageUrl,
        size: selectedVariant.size,
        color: selectedVariant.color,
        price: selectedVariant.price,
        stockQuantity: selectedVariant.stockQuantity,
        quantity: quantity
    });

    showToast('Đã thêm vào giỏ hàng! 🛒', 'success');
    closeVariantModal();
}

function closeVariantModal() {
    document.getElementById('variantModal').classList.remove('active');
    currentModalProduct = null;
    currentModalVariants = [];
    selectedVariant = null;
}

/* ─── Utility ─── */
function formatCurrency(amount) {
    if (!amount && amount !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' ₫';
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

    setTimeout(function () { toast.remove(); }, 3500);
}
