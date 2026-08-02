package com.example.shoestore.repository;

import com.example.shoestore.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {

    Optional<Color> findByColorNameIgnoreCase(String colorName);
}
