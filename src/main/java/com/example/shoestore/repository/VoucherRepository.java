package com.example.shoestore.repository;

import com.example.shoestore.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    boolean existsByCodeIgnoreCase(String code);
}