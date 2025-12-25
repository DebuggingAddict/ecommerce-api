package com.shoppingmall.ecommerceapi.domain.order.repository;

import com.shoppingmall.ecommerceapi.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
