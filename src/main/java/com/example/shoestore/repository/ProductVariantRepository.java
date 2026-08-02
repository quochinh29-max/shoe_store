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
}
