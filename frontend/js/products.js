/**
 * Shoe Store — Products Module
 * CRUD sản phẩm + Tìm kiếm
 */

var editingProductId = null;

document.addEventListener('DOMContentLoaded', function () {
    if (!requireAuth()) return;

    loadProducts();

    // Search
    document.getElementById('searchBtn').addEventListener('click', handleSearch);
    document.getElementById('searchKeyword').addEventListener('keypress', function (e) {
        if (e.key === 'Enter') handleSearch();
    });

    // Add product
    document.getElementById('addProductBtn').addEventListener('click', function () {
        openModal();
    });

    // Form submit
    document.getElementById('productForm').addEventListener('submit', handleSaveProduct);

    // Modal close
    document.getElementById('modalClose').addEventListener('click', closeModal);
    document.getElementById('cancelBtn').addEventListener('click', closeModal);
    document.getElementById('productModal').addEventListener('click', function (e) {
        if (e.target === this) closeModal();
    });

    // Lightbox close
    document.getElementById('lightboxClose').addEventListener('click', closeImageLightbox);
    document.getElementById('imageLightbox').addEventListener('click', function (e) {
        if (e.target === this) closeImageLightbox();
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeImageLightbox();
    });

});

/* ─── Load Products ─── */
async function loadProducts() {
    var tbody = document.getElementById('productsTableBody');
    tbody.innerHTML = '<tr><td colspan="10" class="table-empty"><div class="spinner"></div><p style="margin-top:0.5rem">Đang tải dữ liệu...</p></td></tr>';

    try {
        var products = await apiRequest('/api/products');
        renderTable(products);
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="10" class="table-empty">' +
            '<div class="table-empty-icon">❌</div>' +
            '<p>Không thể tải danh sách sản phẩm</p>' +
            '<p class="text-muted">' + escapeHtml(error.message) + '</p>' +
            '</td></tr>';
    }
}

/* ─── Search ─── */
async function handleSearch() {
    var keyword = document.getElementById('searchKeyword').value.trim();
    var category = document.getElementById('searchCategory').value.trim();
    var brand = document.getElementById('searchBrand').value.trim();

    var tbody = document.getElementById('productsTableBody');
    tbody.innerHTML = '<tr><td colspan="10" class="table-empty"><div class="spinner"></div></td></tr>';

    try {
        var params = new URLSearchParams();
        if (keyword) params.append('keyword', keyword);
        if (category) params.append('category', category);
        if (brand) params.append('brand', brand);

        var products = await apiRequest('/api/products/search?' + params.toString());
        renderTable(products);

        if (products.length === 0) {
            showToast('Không tìm thấy sản phẩm nào', 'error');
        } else {
            showToast('Tìm thấy ' + products.length + ' sản phẩm', 'success');
        }
    } catch (error) {
        showToast('Lỗi tìm kiếm: ' + error.message, 'error');
        loadProducts();
    }
}

/* ─── Render Table ─── */
function renderTable(products) {
    var tbody = document.getElementById('productsTableBody');

    if (!products || products.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="table-empty">' +
            '<div class="table-empty-icon">📦</div>' +
            '<p>Không có sản phẩm nào</p>' +
            '</td></tr>';
        return;
    }

    var html = '';
    for (var i = 0; i < products.length; i++) {
        var p = products[i];
        var stockClass = p.quantity === 0 ? 'out-of-stock' : (p.quantity <= 10 ? 'low-stock' : 'in-stock');
        var stockText = p.quantity === 0 ? 'Hết hàng' : (p.quantity <= 10 ? 'Sắp hết' : 'Còn hàng');
        var thumbHtml = p.imageUrl
            ? '<img class="product-thumb" src="' + escapeHtml(p.imageUrl) + '" alt="' + escapeHtml(p.name) +
            '" onclick="openImageLightbox(\'' + escapeHtml(p.imageUrl).replace(/'/g, "\\'") + '\', \'' + escapeHtml(p.name).replace(/'/g, "\\'") + '\')" ' +
            'onerror="this.replaceWith(Object.assign(document.createElement(\'div\'),{className:\'product-thumb-placeholder\',innerHTML:\'📦\'}))">'
            : '<div class="product-thumb-placeholder">📦</div>';
        html += '<tr>' +
            '<td><strong>' + p.id + '</strong></td>' +
            '<td>' + thumbHtml + '</td>' +
            '<td class="product-name-cell">' + escapeHtml(p.name) + '</td>' +
            '<td>' + escapeHtml(p.brand || '—') + '</td>' +
            '<td>' + escapeHtml(p.category || '—') + '</td>' +
            '<td class="price-cell">' + formatPrice(p.price) + '</td>' +
            '<td><span class="stock-badge ' + stockClass + '">' + p.quantity + ' · ' + stockText + '</span></td>' +
            '<td>' + escapeHtml(p.size || '—') + '</td>' +
            '<td>' + escapeHtml(p.color || '—') + '</td>' +
            '<td><div class="table-actions">' +
            '<button class="btn-icon edit" onclick="openModal(' + p.id + ')" title="Sửa">✏️</button>' +
            '<button class="btn-icon delete" onclick="confirmDelete(' + p.id + ', \'' + escapeHtml(p.name).replace(/'/g, "\\'") + '\')" title="Xóa">🗑️</button>' +
            '</div></td>' +
            '</tr>';
    }

    tbody.innerHTML = html;
}

/* ─── Modal ─── */
function openModal(productId) {
    editingProductId = productId || null;
    var modal = document.getElementById('productModal');
    var title = document.getElementById('modalTitle');
    var form = document.getElementById('productForm');

    form.reset();

    if (editingProductId) {
        title.textContent = '✏️ Sửa sản phẩm';
        loadProductForEdit(editingProductId);
    } else {
        title.textContent = '➕ Thêm sản phẩm mới';
    }

    modal.classList.add('active');
}

async function loadProductForEdit(id) {
    try {
        var product = await apiRequest('/api/products/' + id);

        document.getElementById('productName').value = product.name || '';
        document.getElementById('productDesc').value = product.description || '';
        document.getElementById('productPrice').value = product.price || '';
        document.getElementById('productQty').value = product.quantity != null ? product.quantity : 0;
        document.getElementById('productBrand').value = product.brand || '';
        document.getElementById('productCategory').value = product.category || '';
        document.getElementById('productColor').value = product.color || '';
        document.getElementById('productSize').value = product.size || '';
        document.getElementById('productImageUrl').value = product.imageUrl || '';
    } catch (error) {
        showToast('Không thể tải thông tin sản phẩm: ' + error.message, 'error');
        closeModal();
    }
}

/* ─── Save Product (Create / Update) ─── */
async function handleSaveProduct(e) {
    e.preventDefault();

    var submitBtn = e.target.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span> Đang lưu...';

    var productData = {
        name: document.getElementById('productName').value.trim(),
        description: document.getElementById('productDesc').value.trim(),
        price: parseFloat(document.getElementById('productPrice').value),
        quantity: parseInt(document.getElementById('productQty').value, 10),
        brand: document.getElementById('productBrand').value.trim() || null,
        category: document.getElementById('productCategory').value.trim() || null,
        color: document.getElementById('productColor').value.trim() || null,
        size: document.getElementById('productSize').value.trim() || null,
        imageUrl: document.getElementById('productImageUrl').value.trim() || null
    };

    try {
        if (editingProductId) {
            await apiRequest('/api/products/' + editingProductId, 'PUT', productData);
            showToast('Cập nhật sản phẩm thành công! ✅', 'success');
        } else {
            await apiRequest('/api/products', 'POST', productData);
            showToast('Thêm sản phẩm mới thành công! ✅', 'success');
        }

        closeModal();
        loadProducts();
    } catch (error) {
        var message = getProductErrorMessage(error);
        showToast(message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '💾 Lưu sản phẩm';
    }
}

/* ─── Delete Product ─── */
async function confirmDelete(id, name) {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm "' + name + '"?\n\nHành động này không thể hoàn tác.')) {
        return;
    }

    try {
        await apiRequest('/api/products/' + id, 'DELETE');
        showToast('Đã xóa sản phẩm thành công! 🗑️', 'success');
        loadProducts();
    } catch (error) {
        showToast('Lỗi khi xóa: ' + error.message, 'error');
    }
}

function closeModal() {
    var modal = document.getElementById('productModal');
    modal.classList.remove('active');
    editingProductId = null;
}
/* ─── Image Lightbox ─── */
function openImageLightbox(imageUrl, productName) {
    var lightbox = document.getElementById('imageLightbox');
    var img = document.getElementById('lightboxImg');
    var caption = document.getElementById('lightboxCaption');

    img.src = imageUrl;
    img.alt = productName || 'Ảnh sản phẩm';
    caption.textContent = productName || '';

    lightbox.classList.add('active');
}

function closeImageLightbox() {
    var lightbox = document.getElementById('imageLightbox');
    lightbox.classList.remove('active');
    document.getElementById('lightboxImg').src = '';
}
/* ─── Utility ─── */
function formatPrice(amount) {
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
    if (!container) return;

    var toast = document.createElement('div');
    toast.className = 'toast toast-' + (type || 'success');
    toast.innerHTML = '<span>' + (type === 'error' ? '❌' : '✅') + '</span><span>' + message + '</span>';

    container.appendChild(toast);

    setTimeout(function () {
        if (toast.parentNode) toast.remove();
    }, 3000);
}

function getProductErrorMessage(error) {
    if (error.data) {
        if (error.data.messages && typeof error.data.messages === 'object') {
            return Object.values(error.data.messages).join(', ');
        }
        if (error.data.message) {
            return error.data.message;
        }
    }
    return error.message || 'Có lỗi xảy ra.';
}
