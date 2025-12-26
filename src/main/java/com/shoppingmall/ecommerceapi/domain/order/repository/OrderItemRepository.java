package com.shoppingmall.ecommerceapi.domain.order.repository;

import com.shoppingmall.ecommerceapi.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  /**
   * 특정 상품이 주문 아이템에 존재하는지 확인
   *
   * @param productId 상품 ID
   * @return 존재 여부
   */
  @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.product.id = :productId")
  boolean existsByProductId(@Param("productId") Long productId);

  /**
   * 특정 상품이 주문 아이템에 존재하는지 확인 (삭제되지 않은 주문만)
   *
   * @param productId 상품 ID
   * @return 존재 여부
   */
  @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
      "FROM OrderItem oi " +
      "WHERE oi.product.id = :productId " +
      "AND oi.order.deletedAt IS NULL")
  boolean existsByProductIdAndOrderNotDeleted(@Param("productId") Long productId);

}
