package com.mrer.cleanease.repository;

import com.mrer.cleanease.entity.Enums;
import com.mrer.cleanease.entity.LaundryService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaundryServiceRepository extends JpaRepository<LaundryService, Long> {
    List<LaundryService> findByCategory(Enums.ServiceCategory category);
    List<LaundryService> findByActiveTrue();
    List<LaundryService> findByNameContainingIgnoreCase(String name);


}
