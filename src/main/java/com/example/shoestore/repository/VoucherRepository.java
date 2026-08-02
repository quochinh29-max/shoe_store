package com.example.shoestore.repository;

import com.example.shoestore.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    boolean existsByCodeIgnoreCase(String code);

    // [MỚI] Dùng để tra cứu / áp dụng voucher tại trang giỏ hàng - checkout
    Optional<Voucher> findByCodeIgnoreCase(String code);
}
