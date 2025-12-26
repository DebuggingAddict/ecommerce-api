package com.shoppingmall.ecommerceapi.domain.order.repository;

import com.shoppingmall.ecommerceapi.domain.order.entity.Order;
import com.shoppingmall.ecommerceapi.domain.order.entity.enums.OrderStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

  @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.id = :id AND o.deletedAt IS NULL")
  Optional<Order> findByIdWithItems(@Param("id") Long id);

  @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.deletedAt IS NULL")
  Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderStatus = :orderStatus AND o.deletedAt IS NULL")
  Page<Order> findByUserIdAndOrderStatus(
      @Param("userId") Long userId,
      @Param("orderStatus") OrderStatus orderStatus,
      Pageable pageable
  );

  @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL")
  Page<Order> findAllNotDeleted(Pageable pageable);

  @Query("SELECT o FROM Order o WHERE o.orderStatus = :orderStatus AND o.deletedAt IS NULL")
  Page<Order> findAllByOrderStatus(@Param("orderStatus") OrderStatus orderStatus,
      Pageable pageable);

  @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber AND o.deletedAt IS NULL")
  Optional<Order> findByOrderNumber(@Param("orderNumber") String orderNumber);

  @Query("SELECT COUNT(o) FROM Order o WHERE FUNCTION('DATE', o.createdAt) = CURRENT_DATE")
  Long countTodayOrders();
}
