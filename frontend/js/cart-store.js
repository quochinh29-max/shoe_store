/**
 * Shoe Store — Cart Store
 * Giỏ hàng lưu ở localStorage (client-side), theo từng user (key riêng theo username)
 * để tránh 2 tài khoản dùng chung máy bị lẫn giỏ hàng.
 */

function cartStorageKey() {
    var user = getUserInfo();
    var suffix = user && user.username ? user.username : 'guest';
    return 'shoe_store_cart_' + suffix;
}

function getCart() {
    try {
        var data = localStorage.getItem(cartStorageKey());
        return data ? JSON.parse(data) : [];
    } catch (e) {
        return [];
    }
}

function saveCart(cart) {
    localStorage.setItem(cartStorageKey(), JSON.stringify(cart));
    updateCartBadge();
}

function clearCart() {
    localStorage.removeItem(cartStorageKey());
    updateCartBadge();
}

/**
 * Thêm 1 biến thể vào giỏ. Nếu đã có (cùng variantId) thì cộng dồn số lượng,
 * giới hạn không vượt quá tồn kho (stockQuantity) tại thời điểm thêm.
 */
function addItemToCart(item) {
    var cart = getCart();
    var existing = cart.find(function (c) { return c.variantId === item.variantId; });

    if (existing) {
        existing.quantity = Math.min(existing.quantity + item.quantity, item.stockQuantity);
    } else {
        cart.push(item);
    }

    saveCart(cart);
    return cart;
}

function removeItemFromCart(variantId) {
    var cart = getCart().filter(function (c) { return c.variantId !== variantId; });
    saveCart(cart);
    return cart;
}

function updateItemQuantity(variantId, quantity) {
    var cart = getCart();
    var item = cart.find(function (c) { return c.variantId === variantId; });
    if (item) {
        item.quantity = Math.max(1, Math.min(quantity, item.stockQuantity || quantity));
        saveCart(cart);
    }
    return cart;
}

function getCartCount() {
    return getCart().reduce(function (sum, c) { return sum + c.quantity; }, 0);
}

function getCartSubtotal() {
    return getCart().reduce(function (sum, c) { return sum + (c.price * c.quantity); }, 0);
}

function updateCartBadge() {
    var badge = document.getElementById('cartBadge');
    if (!badge) return;
    var count = getCartCount();
    if (count > 0) {
        badge.textContent = count > 99 ? '99+' : count;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', updateCartBadge);
