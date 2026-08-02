package com.example.shoestore.controller;

import com.example.shoestore.dto.ProductDTO;
import com.example.shoestore.dto.ProductVariantDTO;
import com.example.shoestore.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products
     * Get all products
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * GET /api/products/{id}
     * Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * [MỚI] GET /api/products/{id}/variants
     * Danh sách biến thể (size/màu/tồn kho) — dùng cho trang mua sắm (shop.html)
     * để khách chọn size/màu trước khi thêm vào giỏ hàng.
     */
    @GetMapping("/{id}/variants")
    public ResponseEntity<List<ProductVariantDTO>> getProductVariants(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductVariants(id));
    }

    /**
     * POST /api/products
     * Create a new product
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO dto) {
        ProductDTO created = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/products/{id}
     * Update an existing product
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Integer id,
                                                    @Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    /**
     * DELETE /api/products/{id}
     * Delete a product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Product with ID " + id + " deleted successfully"
        ));
    }

    /**
     * GET /api/products/search?keyword=&category=&brand=
     * Search products
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand) {
        return ResponseEntity.ok(productService.searchProducts(keyword, category, brand));
    }
}
