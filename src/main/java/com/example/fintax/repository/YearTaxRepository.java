package com.example.fintax.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.example.fintax.entity.YearTax;

public interface YearTaxRepository extends JpaRepository<YearTax, String> {
  // save(), findById(), delete() 숨어있음
}
