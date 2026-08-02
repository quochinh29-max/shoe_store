/**
 * Shoe Store — Reports Module
 * Gọi API /api/reports/*, render thẻ tổng quan, biểu đồ doanh thu theo ngày, top sản phẩm
 */

var currentRangeDays = 30;

document.addEventListener('DOMContentLoaded', function () {
    loadReportData(currentRangeDays);

    var rangeBtns = document.querySelectorAll('.range-btn');
    rangeBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            rangeBtns.forEach(function (b) { b.classList.remove('active'); });
            btn.classList.add('active');
            currentRangeDays = parseInt(btn.getAttribute('data-days'), 10);
            loadReportData(currentRangeDays);
        });
    });
});

async function loadReportData(days) {
    try {
        var summaryPromise = apiRequest('/api/reports/summary?days=' + days);
        var dailyPromise = apiRequest('/api/reports/daily?days=' + days);
        var topProductsPromise = apiRequest('/api/reports/top-products?days=' + days + '&limit=5');

        var summary = await summaryPromise;
        renderSummary(summary);

        var daily = await dailyPromise;
        renderChart(daily);

        var topProducts = await topProductsPromise;
        renderTopProducts(topProducts);
    } catch (error) {
        console.error('Report load error:', error);
        showToast('Không thể tải dữ liệu báo cáo. Vui lòng thử lại.', 'error');
    }
}

function renderSummary(summary) {
    document.getElementById('repTotalRevenue').textContent = formatCurrency(summary.totalRevenue);
    document.getElementById('repTotalOrders').textContent = summary.totalOrders;
    document.getElementById('repCompletedOrders').textContent = summary.completedOrders;
    document.getElementById('repAvgOrder').textContent = formatCurrency(summary.averageOrderValue);

    var pct = summary.totalOrders > 0
        ? Math.round((summary.completedOrders / summary.totalOrders) * 100)
        : 0;
    document.getElementById('repCompletedPct').textContent = pct + '% tổng đơn';

    document.getElementById('repPending').textContent = summary.pendingOrders;
    document.getElementById('repShipping').textContent = summary.shippingOrders;
    document.getElementById('repCompleted2').textContent = summary.completedOrders;
    document.getElementById('repCancelled').textContent = summary.cancelledOrders;
}

function renderChart(dailyData) {
    var container = document.getElementById('revenueChart');

    if (!dailyData || dailyData.length === 0) {
        container.innerHTML = '<div class="table-empty"><div class="table-empty-icon">📉</div><p>Chưa có dữ liệu đơn hàng trong khoảng thời gian này</p></div>';
        return;
    }

    var maxRevenue = Math.max.apply(null, dailyData.map(function (d) { return d.revenue; }));
    if (maxRevenue === 0) maxRevenue = 1;

    var barsHtml = '<div class="chart-bars">';
    dailyData.forEach(function (d) {
        var heightPct = Math.max((d.revenue / maxRevenue) * 100, d.revenue > 0 ? 3 : 0);
        var dateLabel = formatShortDate(d.date);
        barsHtml +=
            '<div class="chart-bar-col" title="' + dateLabel + ': ' + formatCurrency(d.revenue) + ' (' + d.orderCount + ' đơn)">' +
            '<div class="chart-bar" style="height:' + heightPct + '%"></div>' +
            '<div class="chart-bar-label">' + dateLabel + '</div>' +
            '</div>';
    });
    barsHtml += '</div>';

    container.innerHTML = barsHtml;
}

function renderTopProducts(products) {
    var tbody = document.getElementById('topProductsBody');

    if (!products || products.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="table-empty">' +
            '<div class="table-empty-icon">🏆</div>' +
            '<p>Chưa có sản phẩm nào được bán trong khoảng thời gian này</p>' +
            '</td></tr>';
        return;
    }

    var medals = ['🥇', '🥈', '🥉'];
    var html = '';
    products.forEach(function (p, index) {
        var rank = medals[index] || ('#' + (index + 1));
        html += '<tr>' +
            '<td><strong>' + rank + '</strong></td>' +
            '<td class="product-name-cell">' + escapeHtml(p.productName) + '</td>' +
            '<td>' + p.totalSold + ' đôi</td>' +
            '<td>' + formatCurrency(p.revenue) + '</td>' +
            '</tr>';
    });

    tbody.innerHTML = html;
}

/* ─── Utility ─── */
function formatCurrency(amount) {
    if (!amount && amount !== 0) return '0 ₫';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' ₫';
}

function formatShortDate(isoDate) {
    // isoDate dạng "yyyy-MM-dd" trả về từ backend
    var parts = isoDate.split('-');
    if (parts.length !== 3) return isoDate;
    return parts[2] + '/' + parts[1];
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
