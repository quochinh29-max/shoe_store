package com.example.shoestore.repository;

import com.example.shoestore.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    /**
     * Danh sách biến thể (size/màu/tồn kho) của 1 sản phẩm, dùng cho trang mua sắm.
     */
    @Query("SELECT v FROM ProductVariant v " +
            "LEFT JOIN FETCH v.size " +
            "LEFT JOIN FETCH v.color " +
            "LEFT JOIN FETCH v.product " +
            "WHERE v.product.id = :productId " +
            "ORDER BY v.id ASC")
    List<ProductVariant> findByProductIdWithRefs(@Param("productId") Integer productId);

    /**
     * [FIXED] Bản KHÔNG JOIN FETCH size/color — chỉ dùng để kiểm tra "sản phẩm đã có
     * biến thể chưa" (đếm/đồng bộ trong ProductService, DataInitializer).
     * Nếu dùng findByProductIdWithRefs() cho việc này, một dòng product_variants có
     * size_id/color_id trỏ đến bản ghi KHÔNG CÒN tồn tại (dữ liệu rác) sẽ làm Hibernate
     * ném FetchNotFoundException và crash cả app ngay lúc khởi động.
     */
    List<ProductVariant> findByProductId(Integer productId);

    /**
     * [MỚI] Dùng để sinh SKU mặc định không trùng khi tự động tạo/đồng bộ biến thể
     * từ form Thêm/Sửa sản phẩm (size/màu dạng text) ở trang Admin.
     */
    boolean existsBySku(String sku);

    /**
     * [FIXED] Tổng tồn kho THẬT của 1 sản phẩm = tổng stock_quantity của mọi biến thể.
     * Trước đây trang Admin hiển thị cột products.quantity (legacy) — cột này KHÔNG
     * được cập nhật khi khách đặt hàng (OrderService chỉ trừ product_variants.stock_quantity),
     * nên sau khi khách mua, Admin vẫn thấy số lượng cũ. Dùng hàm này làm nguồn sự thật duy nhất.
     */
    @Query("SELECT COALESCE(SUM(v.stockQuantity), 0) FROM ProductVariant v WHERE v.product.id = :productId")
    Integer sumStockQuantityByProductId(@Param("productId") Integer productId);

    /**
     * [FIXED] Bản gộp nhóm cho NHIỀU sản phẩm cùng lúc (dùng ở danh sách/tìm kiếm) để tránh
     * N+1 query (gọi sumStockQuantityByProductId riêng lẻ cho từng sản phẩm trong vòng lặp).
     */
    @Query("SELECT v.product.id, COALESCE(SUM(v.stockQuantity), 0) FROM ProductVariant v GROUP BY v.product.id")
    List<Object[]> sumStockQuantityGroupByProduct();
}