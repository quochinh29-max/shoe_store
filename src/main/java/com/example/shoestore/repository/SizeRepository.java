package com.example.shoestore.repository;

import com.example.shoestore.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {

    Optional<Size> findBySizeValue(String sizeValue);
}
