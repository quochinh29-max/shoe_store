package com.example.shoestore.repository;

import com.example.shoestore.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByOrderStatus(String orderStatus);
    long countByOrderStatus(String orderStatus);

    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Long countByUserId(Integer userId);

    /**
     * Tổng tiền khách đã chi cho các đơn COMPLETED (dùng cho thống kê khách hàng).
     */
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o " +
            "WHERE o.user.id = :userId AND o.orderStatus = 'COMPLETED'")
    BigDecimal sumSpentByUserId(@Param("userId") Integer userId);

    /**
     * Danh sách đơn hàng kèm thông tin user (fetch join để tránh N+1 khi map sang DTO).
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.user ORDER BY o.createdAt DESC")
    List<Order> findAllWithUser();

    /**
     * Chi tiết 1 đơn hàng kèm user, danh sách sản phẩm (variant/product/size/color) để tránh LazyInitializationException.
     */
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.variant v " +
            "LEFT JOIN FETCH v.product " +
            "LEFT JOIN FETCH v.size " +
            "LEFT JOIN FETCH v.color " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Integer id);

    /**
     * Tổng doanh thu (chỉ tính đơn COMPLETED) trong khoảng thời gian.
     */
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o " +
            "WHERE o.orderStatus = 'COMPLETED' " +
            "AND o.createdAt >= :from AND o.createdAt <= :to")
    BigDecimal sumRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Đếm tổng số đơn hàng trong khoảng thời gian (bất kể trạng thái).
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from AND o.createdAt <= :to")
    long countOrdersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Đếm số đơn theo 1 trạng thái cụ thể trong khoảng thời gian.
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status " +
            "AND o.createdAt >= :from AND o.createdAt <= :to")
    long countByStatusBetween(@Param("status") String status,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to);

    /**
     * Doanh thu + số đơn theo từng ngày trong khoảng thời gian (dùng cho biểu đồ).
     * Trả về mảng: [0]=ngày (String yyyy-MM-dd), [1]=số đơn, [2]=doanh thu (chỉ đơn COMPLETED)
     */
    @Query(value =
            "SELECT DATE(o.created_at) AS report_date, " +
                    "       COUNT(*) AS order_count, " +
                    "       COALESCE(SUM(CASE WHEN o.order_status = 'COMPLETED' THEN o.final_amount ELSE 0 END), 0) AS revenue " +
                    "FROM orders o " +
                    "WHERE o.created_at >= :from AND o.created_at <= :to " +
                    "GROUP BY DATE(o.created_at) " +
                    "ORDER BY report_date ASC",
            nativeQuery = true)
    List<Object[]> findDailyRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Top sản phẩm bán chạy nhất theo số lượng đã bán, trong khoảng thời gian.
     * Trả về: [0]=product_id, [1]=tên sản phẩm, [2]=tổng số lượng bán, [3]=tổng doanh thu từ sản phẩm đó
     */
    @Query(value =
            "SELECT p.id, p.name, SUM(od.quantity) AS total_sold, " +
                    "       SUM(od.quantity * od.unit_price) AS product_revenue " +
                    "FROM order_details od " +
                    "JOIN orders o ON od.order_id = o.id " +
                    "JOIN product_variants pv ON od.variant_id = pv.id " +
                    "JOIN products p ON pv.product_id = p.id " +
                    "WHERE o.order_status = 'COMPLETED' " +
                    "AND o.created_at >= :from AND o.created_at <= :to " +
                    "GROUP BY p.id, p.name " +
                    "ORDER BY total_sold DESC " +
                    "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopSellingProducts(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          @Param("limit") int limit);
}