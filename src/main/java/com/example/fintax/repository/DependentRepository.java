package com.example.fintax.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fintax.entity.Dependent;

public interface DependentRepository extends JpaRepository<Dependent, Long> {
  //기본 CRUD 기능
}
