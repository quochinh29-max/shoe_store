package com.example.shoestore.service;

import com.example.shoestore.dto.ProductDTO;
import com.example.shoestore.dto.ProductVariantDTO;
import com.example.shoestore.entity.Color;
import com.example.shoestore.entity.Product;
import com.example.shoestore.entity.ProductVariant;
import com.example.shoestore.entity.Size;
import com.example.shoestore.exception.ResourceNotFoundException;
import com.example.shoestore.repository.ColorRepository;
import com.example.shoestore.repository.ProductRepository;
import com.example.shoestore.repository.ProductVariantRepository;
import com.example.shoestore.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    // [MỚI]
    private final ProductVariantRepository productVariantRepository;
    // [FIXED] Cần để tự tạo/đồng bộ biến thể (size/màu) khi thêm/sửa sản phẩm ở trang Admin —
    // trước đây form Admin chỉ lưu size/màu dạng chuỗi trên bảng products, không tạo dòng nào
    // trong product_variants, nên trang mua sắm không có gì để khách chọn -> không thêm được vào giỏ.
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;

    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAllWithRefs();
        Map<Integer, Integer> stockMap = loadStockMap();
        return products.stream()
                .map(p -> toDTO(p, stockMap))
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toDTO(product, realStockQuantity(product.getId(), product));
    }

    /**
     * [MỚI] Danh sách biến thể (size/màu/giá/tồn kho) của 1 sản phẩm.
     * Dùng cho trang mua sắm (shop.html) để khách chọn size/màu trước khi thêm vào giỏ.
     */
    @Transactional(readOnly = true)
    public List<ProductVariantDTO> getProductVariants(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        return productVariantRepository.findByProductIdWithRefs(productId)
                .stream()
                .map(v -> ProductVariantDTO.builder()
                        .id(v.getId())
                        .size(v.getSize() != null ? v.getSize().getSizeValue() : "-")
                        .color(v.getColor() != null ? v.getColor().getColorName() : "-")
                        .price(v.getPrice() != null ? v.getPrice() : v.getProduct().getPrice())
                        .stockQuantity(v.getStockQuantity() != null ? v.getStockQuantity() : 0)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Create a new product
     */
    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = toEntity(dto);
        Product saved = productRepository.save(product);
        // [FIXED] Tự tạo 1 biến thể (size/màu) mặc định để khách hàng có thể mua ngay
        syncDefaultVariant(saved, dto);
        return toDTO(saved, realStockQuantity(saved.getId(), saved));
    }

    /**
     * Update an existing product
     */
    @Transactional
    public ProductDTO updateProduct(Integer id, ProductDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setBasePrice(dto.getPrice());
        existing.setQuantity(dto.getQuantity());
        existing.setBrand(dto.getBrand());
        existing.setSize(dto.getSize());
        existing.setColor(dto.getColor());
        existing.setCategory(dto.getCategory());
        existing.setImageUrl(dto.getImageUrl());
        existing.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(existing);
        // [FIXED] Đồng bộ biến thể mặc định theo size/màu/giá/kho vừa sửa
        syncDefaultVariant(updated, dto);
        return toDTO(updated, realStockQuantity(updated.getId(), updated));
    }

    /**
     * Delete a product by ID
     */
    @Transactional
    public void deleteProduct(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Search products by keyword, category, brand
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword, String category, String brand) {
        List<Product> products = productRepository.searchWithRefs(keyword, category, brand);
        Map<Integer, Integer> stockMap = loadStockMap();
        return products.stream()
                .map(p -> toDTO(p, stockMap))
                .collect(Collectors.toList());
    }

    // ---- Stock helpers ----
    // [FIXED] products.quantity là cột legacy, KHÔNG được cập nhật khi khách đặt hàng
    // (OrderService chỉ trừ product_variants.stock_quantity). Nguồn sự thật cho số lượng
    // hiển thị ở Admin phải là tổng stock_quantity của các biến thể.

    /** Gộp nhóm 1 lần cho danh sách/tìm kiếm, tránh N+1 query. */
    private Map<Integer, Integer> loadStockMap() {
        return productVariantRepository.sumStockQuantityGroupByProduct().stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> ((Number) row[1]).intValue()));
    }

    /** Tra tồn kho thật của 1 sản phẩm; fallback về products.quantity nếu chưa có biến thể nào. */
    private Integer realStockQuantity(Integer productId, Product product) {
        Integer sum = productVariantRepository.sumStockQuantityByProductId(productId);
        if (sum != null && sum > 0) {
            return sum;
        }
        boolean hasVariant = !productVariantRepository.findByProductId(productId).isEmpty();
        if (hasVariant) {
            return 0;
        }
        return product.getQuantity() != null ? product.getQuantity() : 0;
    }

    // ---- Variant sync helpers ----
    // [MỚI] Trang Admin chỉ quản lý 1 size/1 màu dạng text cho mỗi sản phẩm (form đơn giản),
    // trong khi trang mua sắm của khách lại lấy option từ bảng product_variants.
    // Hàm dưới đây tự động giữ cho product_variants luôn có ít nhất 1 biến thể tương ứng,
    // để khách hàng luôn chọn được size/màu và thêm vào giỏ hàng.

    /**
     * Đồng bộ 1 biến thể mặc định theo size/màu/giá/kho trên form Admin.
     * - Nếu sản phẩm chưa có biến thể nào -> tạo mới.
     * - Nếu chỉ có đúng 1 biến thể (trường hợp phổ biến với sản phẩm tạo qua Admin) -> cập nhật theo form.
     * - Nếu sản phẩm đã có NHIỀU biến thể (vd: dữ liệu seed nhiều size/màu) -> giữ nguyên,
     *   không tự ý ghi đè để tránh mất dữ liệu biến thể chi tiết đã có.
     */
    private void syncDefaultVariant(Product product, ProductDTO dto) {
        if (dto.getSize() == null || dto.getSize().isBlank()
                || dto.getColor() == null || dto.getColor().isBlank()) {
            // Không đủ thông tin size/màu để tạo biến thể có thể mua được
            return;
        }

        List<ProductVariant> variants = productVariantRepository.findByProductIdWithRefs(product.getId());
        Size size = findOrCreateSize(dto.getSize().trim());
        Color color = findOrCreateColor(dto.getColor().trim());
        Integer stock = dto.getQuantity() != null ? dto.getQuantity() : 0;

        if (variants.isEmpty()) {
            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .size(size)
                    .color(color)
                    .sku(generateUniqueSku(product, size, color))
                    .price(dto.getPrice())
                    .stockQuantity(stock)
                    .build();
            productVariantRepository.save(variant);
        } else if (variants.size() == 1) {
            ProductVariant variant = variants.get(0);
            variant.setSize(size);
            variant.setColor(color);
            variant.setPrice(dto.getPrice());
            variant.setStockQuantity(stock);
            productVariantRepository.save(variant);
        }
    }

    private Size findOrCreateSize(String sizeValue) {
        return sizeRepository.findBySizeValue(sizeValue)
                .orElseGet(() -> sizeRepository.save(Size.builder().sizeValue(sizeValue).build()));
    }

    private Color findOrCreateColor(String colorName) {
        return colorRepository.findByColorNameIgnoreCase(colorName)
                .orElseGet(() -> colorRepository.save(Color.builder().colorName(colorName).build()));
    }

    private String generateUniqueSku(Product product, Size size, Color color) {
        String colorPrefix = color.getColorName().length() >= 3
                ? color.getColorName().substring(0, 3)
                : color.getColorName();
        String base = ("SP" + product.getId() + "-" + size.getSizeValue() + "-" + colorPrefix)
                .toUpperCase()
                .replaceAll("\\s+", "");

        String sku = base;
        int suffix = 1;
        while (productVariantRepository.existsBySku(sku)) {
            sku = base + "-" + (suffix++);
        }
        return sku;
    }

    // ---- Mapper helpers ----

    /** Overload dùng cho danh sách/tìm kiếm — tra tồn kho từ map đã gộp nhóm sẵn. */
    private ProductDTO toDTO(Product product, Map<Integer, Integer> stockMap) {
        Integer stock = stockMap.get(product.getId());
        if (stock == null) {
            // Sản phẩm chưa có biến thể nào -> fallback về cột legacy
            stock = product.getQuantity() != null ? product.getQuantity() : 0;
        }
        return toDTO(product, stock);
    }

    private ProductDTO toDTO(Product product, Integer realQuantity) {
        // Ưu tiên cột varchar legacy; nếu null thì fallback sang quan hệ brandRef/categoryRef
        String brand = product.getBrand() != null
                ? product.getBrand()
                : (product.getBrandRef() != null ? product.getBrandRef().getName() : null);

        String category = product.getCategory() != null
                ? product.getCategory()
                : (product.getCategoryRef() != null ? product.getCategoryRef().getName() : null);

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(realQuantity)
                .brand(brand)
                .size(product.getSize())
                .color(product.getColor())
                .category(category)
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private Product toEntity(ProductDTO dto) {
        String slug = dto.getName().toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                + "-" + System.currentTimeMillis();
        return Product.builder()
                .name(dto.getName())
                .slug(slug)
                .description(dto.getDescription())
                .basePrice(dto.getPrice())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .brand(dto.getBrand())
                .size(dto.getSize())
                .color(dto.getColor())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .build();
    }
}