package com.example.shoestore.service;

import com.example.shoestore.dto.ProductDTO;
import com.example.shoestore.entity.Product;
import com.example.shoestore.exception.ResourceNotFoundException;
import com.example.shoestore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toDTO(product);
    }

    /**
     * Create a new product
     */
    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = toEntity(dto);
        Product saved = productRepository.save(product);
        return toDTO(saved);
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
        existing.setQuantity(dto.getQuantity());
        existing.setBrand(dto.getBrand());
        existing.setSize(dto.getSize());
        existing.setColor(dto.getColor());
        existing.setCategory(dto.getCategory());
        existing.setImageUrl(dto.getImageUrl());
        existing.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(existing);
        return toDTO(updated);
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
        return productRepository.search(keyword, category, brand)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ---- Mapper helpers ----

    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .brand(product.getBrand())
                .size(product.getSize())
                .color(product.getColor())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private Product toEntity(ProductDTO dto) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
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
