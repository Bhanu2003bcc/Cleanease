package com.mrer.cleanease.repository;

import com.mrer.cleanease.entity.Order;
import com.mrer.cleanease.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
    List<OrderItems> findByOrder(Order order);

}
