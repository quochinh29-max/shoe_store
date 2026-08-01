package com.example.shoestore.repository;

import com.example.shoestore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByBrandIgnoreCase(String brand);

    @Query("SELECT p FROM Product p WHERE p.quantity > 0")
    List<Product> findInStockProducts();

    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:category IS NULL OR LOWER(p.category) = LOWER(:category)) AND " +
           "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand))")
    List<Product> search(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("brand") String brand
    );

    /**
     * Load tất cả sản phẩm kèm brandRef và categoryRef (eager) để tránh LazyInitializationException
     * và đảm bảo brand/category luôn hiển thị đúng kể cả khi cột varchar legacy còn null.
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.brandRef " +
           "LEFT JOIN FETCH p.categoryRef")
    List<Product> findAllWithRefs();

    /**
     * Search mở rộng: tìm theo cột varchar brand/category VÀ theo tên trong bảng quan hệ
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.brandRef b " +
           "LEFT JOIN FETCH p.categoryRef c " +
           "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR LOWER(COALESCE(p.category, '')) = LOWER(:category) " +
           "     OR LOWER(COALESCE(c.name, '')) = LOWER(:category)) " +
           "AND (:brand IS NULL OR LOWER(COALESCE(p.brand, '')) = LOWER(:brand) " +
           "     OR LOWER(COALESCE(b.name, '')) = LOWER(:brand))")
    List<Product> searchWithRefs(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("brand") String brand
    );
}
