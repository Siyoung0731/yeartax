package com.example.fintax.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.fintax.entity.YearTax;

public interface YearTaxRepository extends JpaRepository<YearTax, String> {
  // save(), findById(), delete() 숨어있음

  // 'ROLE_USER' 권한만 삭제
  @Modifying
  @Transactional
  @Query("DELETE FROM YearTax y WHERE y.role = 'ROLE_USER'")
  void deleteEmployees();
}
