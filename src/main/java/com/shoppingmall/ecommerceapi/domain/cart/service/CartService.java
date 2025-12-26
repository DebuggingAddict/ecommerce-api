package com.shoppingmall.ecommerceapi.domain.cart.service;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.cart.converter.CartConverter;
import com.shoppingmall.ecommerceapi.domain.cart.dto.AddCartItemRequest;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartItemResponse;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartResponse;
import com.shoppingmall.ecommerceapi.domain.cart.entity.Cart;
import com.shoppingmall.ecommerceapi.domain.cart.entity.CartItem;
import com.shoppingmall.ecommerceapi.domain.cart.exception.CartErrorCode;
import com.shoppingmall.ecommerceapi.domain.cart.repository.CartRepository;
import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

  private final CartRepository cartRepository;
  private final ProductService productService;

  @Transactional(readOnly = true)
  public Cart getCartByUserId(Long userId) {
    return cartRepository.findByUserId(userId)
        .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));
  }

  public Cart createCartForUser(Long userId) {
    if (cartRepository.existsByUserId(userId)) {
      throw new BusinessException(CartErrorCode.CART_ALREADY_EXISTS);
    }
    return cartRepository.save(Cart.builder().userId(userId).build());
  }

  /**
   * 장바구니 조회 + Product 정보 조합 후 CartResponse 반환.
   */
  @Transactional(readOnly = true)
  public CartResponse getCartResponse(Long userId) {
    Cart cart = getCartByUserId(userId);

    List<CartItemResponse> itemResponses = cart.getItems().stream()
        .map(item -> {
          Product product = productService.findProductEntityById(item.getProductId());
          return CartConverter.toCartItemResponse(
              item,
              product.getName(),
              product.getPrice()
          );
        })
        .toList();

    return CartConverter.toCartResponse(cart, itemResponses);
  }

  /**
   * 정책: 이미 담긴 상품이면 CART_ITEM_ALREADY_EXISTS
   */
  public CartItem addItem(Long userId, AddCartItemRequest request) {
    Cart cart = getCartByUserId(userId);

    if (request.getQuantity() <= 0) {
      throw new BusinessException(CartErrorCode.CART_ITEM_INVALID_QUANTITY);
    }

    // 1) 상품 존재/유효성 검증
    Product product = productService.findProductEntityById(request.getProductId());

    // 2) 판매 상태 검증
    ProductStatus status = product.getStatus();
    if (status == ProductStatus.STOP_SALE) {
      throw new BusinessException(CartErrorCode.CART_ITEM_PRODUCT_NOT_FOR_SALE);
    }
    if (status == ProductStatus.SOLD_OUT) {
      throw new BusinessException(CartErrorCode.CART_ITEM_OUT_OF_STOCK);
    }

    // 3) 장바구니 중복 검증
    boolean exists = cart.getItems().stream()
        .anyMatch(i -> i.getProductId().equals(request.getProductId()));
    if (exists) {
      throw new BusinessException(CartErrorCode.CART_ITEM_ALREADY_EXISTS);
    }

    // 4) 담기
    CartItem item = CartConverter.toEntity(request);
    cart.addItem(item);

    return item;
  }


  public CartItem changeQuantity(Long userId, Long cartItemId, int quantity) {
    Cart cart = getCartByUserId(userId);

    if (quantity <= 0) {
      throw new BusinessException(CartErrorCode.CART_ITEM_INVALID_QUANTITY);
    }

    CartItem item = cart.getItems().stream()
        .filter(i -> cartItemId.equals(i.getId()))// ID 기준
        .findFirst()
        .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));

    item.changeQuantity(quantity);
    return item;
  }

  public void removeItem(Long userId, Long productId) {
    Cart cart = getCartByUserId(userId);

    CartItem item = cart.getItems().stream()
        .filter(i -> i.getProductId().equals(productId))
        .findFirst()
        .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));

    cart.removeItem(item);
  }

  public void clearCart(Long userId) {
    Cart cart = getCartByUserId(userId);

    if (cart.getItems().isEmpty()) {
      throw new BusinessException(CartErrorCode.CART_ALREADY_EMPTY);
    }

    try {
      cart.clearItems();
    } catch (Exception e) {
      throw new BusinessException(CartErrorCode.CART_CLEANUP_FAILED, e.getMessage());
    }
  }
}
