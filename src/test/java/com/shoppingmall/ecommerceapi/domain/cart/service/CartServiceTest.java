package com.shoppingmall.ecommerceapi.domain.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.cart.dto.AddCartItemRequest;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartResponse;
import com.shoppingmall.ecommerceapi.domain.cart.entity.Cart;
import com.shoppingmall.ecommerceapi.domain.cart.entity.CartItem;
import com.shoppingmall.ecommerceapi.domain.cart.exception.CartErrorCode;
import com.shoppingmall.ecommerceapi.domain.cart.repository.CartRepository;
import com.shoppingmall.ecommerceapi.domain.product.entity.Product;
import com.shoppingmall.ecommerceapi.domain.product.entity.enums.ProductStatus;
import com.shoppingmall.ecommerceapi.domain.product.service.ProductService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock
  CartRepository cartRepository;

  @Mock
  ProductService productService;

  @InjectMocks
  CartService cartService;

  @Test
  @DisplayName("getCartByUserId: 존재하면 Cart 반환")
  void getCartByUserId_success() {
    // given
    Long userId = 1L;
    Cart cart = Cart.builder().userId(userId).build();
    given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));

    // when
    Cart result = cartService.getCartByUserId(userId);

    // then
    assertThat(result).isSameAs(cart);
  }

  @Test
  @DisplayName("getCartByUserId: 없으면 CART_NOT_FOUND 예외")
  void getCartByUserId_notFound() {
    // given
    Long userId = 1L;
    given(cartRepository.findByUserId(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> cartService.getCartByUserId(userId))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(CartErrorCode.CART_NOT_FOUND);
  }

  @Test
  @DisplayName("createCartForUser: 이미 존재하면 CART_ALREADY_EXISTS 예외")
  void createCartForUser_alreadyExists() {
    // given
    Long userId = 1L;
    given(cartRepository.existsByUserId(userId)).willReturn(true);

    // when & then
    assertThatThrownBy(() -> cartService.createCartForUser(userId))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(CartErrorCode.CART_ALREADY_EXISTS);

    verify(cartRepository, never()).save(org.mockito.ArgumentMatchers.any(Cart.class));
  }

  @Test
  @DisplayName("createCartForUser: 없으면 저장 후 반환")
  void createCartForUser_success() {
    // given
    Long userId = 1L;
    given(cartRepository.existsByUserId(userId)).willReturn(false);

    Cart saved = Cart.builder().userId(userId).build();
    given(cartRepository.save(org.mockito.ArgumentMatchers.any(Cart.class))).willReturn(saved);

    // when
    Cart result = cartService.createCartForUser(userId);

    // then
    assertThat(result).isSameAs(saved);
    verify(cartRepository).save(org.mockito.ArgumentMatchers.any(Cart.class));
  }

  @Test
  @DisplayName("getCartResponse: CartItem의 productId로 Product 조회해 응답에 productName/productPrice를 포함")
  void getCartResponse_success() {
    // given
    Long userId = 1L;

    Cart cart = Cart.builder().userId(userId).build();
    cart.addItem(CartItem.builder().productId(100L).quantity(2).build());

    given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));

    Product product = Product.builder()
        .name("테스트상품")
        .price(1000)
        .status(ProductStatus.FOR_SALE)
        .build();

    given(productService.findProductEntityById(100L)).willReturn(product);

    // when
    CartResponse response = cartService.getCartResponse(userId);

    // then
    assertThat(response.getUserId()).isEqualTo(userId);
    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().get(0).getProductId()).isEqualTo(100L);
    assertThat(response.getItems().get(0).getProductName()).isEqualTo("테스트상품");
    assertThat(response.getItems().get(0).getProductPrice()).isEqualTo(1000);
    assertThat(response.getItems().get(0).getProductQuantity()).isEqualTo(2);

    verify(productService).findProductEntityById(100L);
  }

  @Test
  @DisplayName("addItem: quantity <= 0 이면 CART_ITEM_INVALID_QUANTITY 예외")
  void addItem_invalidQuantity() {
    // given
    Long userId = 1L;
    AddCartItemRequest req = AddCartItemRequest.builder().productId(1L).quantity(0).build();

    Cart cart = Cart.builder().userId(userId).build();
    given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));

    // when & then
    assertThatThrownBy(() -> cartService.addItem(userId, req))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(CartErrorCode.CART_ITEM_INVALID_QUANTITY);

    verify(productService, never()).findProductEntityById(org.mockito.ArgumentMatchers.anyLong());
  }

  @Test
  @DisplayName("addItem: STOP_SALE 상품이면 CART_ITEM_PRODUCT_NOT_FOR_SALE 예외")
  void addItem_stopSale() {
    // given
    Long userId = 1L;

    AddCartItemRequest req = AddCartItemRequest.builder().productId(10L).quantity(1).build();
    Cart cart = Cart.builder().userId(userId).build();

    given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));

    Product product = Product.builder()
        .name("중지상품")
        .price(1000)
        .status(ProductStatus.STOP_SALE)
        .build();

    given(productService.findProductEntityById(10L)).willReturn(product);

    // when & then
    assertThatThrownBy(() -> cartService.addItem(userId, req))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(CartErrorCode.CART_ITEM_PRODUCT_NOT_FOR_SALE);
  }

  @Test
  @DisplayName("addItem: SOLD_OUT 상품이면 CART_ITEM_OUT_OF_STOCK 예외")
  void addItem_soldOut() {
    // given
    Long userId = 1L;

    AddCartItemRequest req = AddCartItemRequest.builder().productId(10L).quantity(1).build();
    Cart cart = Cart.builder().userId(userId).build();

    given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));

    Product product = Product.builder()
        .name("품절상품")
        .price(1000)
        .status(ProductStatus.SOLD_OUT)
        .build();

    given(productService.findProductEntityById(10L)).willReturn(product);

    // when & then
    assertThatThrownBy(() -> cartService.addItem(userId, req))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(CartErrorCode.CART_ITEM_OUT_OF_STOCK);
  }

  @Test
  @DisplayName("addItem: 이미 담긴 상품이면 CART_ITEM_ALREADY_EXISTS 예외")
  void addItem_alreadyExists() {
    // given
    Long userId = 1L;

    Cart cart = Cart.builder().userId(userId).build();
    cart.addItem(CartItem.builder().productId(10L).quantity(1).build());

    given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));

    AddCartItemRequest req = AddCartItemRequest.builder().productId(10L).quantity(1).build();

    Product product = Product.builder()
        .name("상품")
        .price(1000)
        .status(ProductStatus.FOR_SALE)
        .build();

    given(productService.findProductEntityById(10L)).willReturn(product);

    // when & then
    assertThatThrownBy(() -> cartService.addItem(userId, req))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(CartErrorCode.CART_ITEM_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("changeQuantity: 0 이하이면 CART_ITEM_INVALID_QUANTITY 예외")
  void changeQuantity_invalidQuantity() {
    // given
    Long userId = 1L;
    Long cartItemId = 1L;

    Cart cart = Cart.builder().userId(userId).build();
    cart.addItem(CartItem.builder().productId(10L).quantity(1).build());

    given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));

    // when & then
    assertThatThrownBy(() -> cartService.changeQuantity(userId, cartItemId, 0))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(CartErrorCode.CART_ITEM_INVALID_QUANTITY);
  }

  @Test
  @DisplayName("changeQuantity: 해당 cartItemId가 없으면 CART_ITEM_NOT_FOUND 예외")
  void changeQuantity_notFound() {
    // given
    Long userId = 1L;
    Long cartItemId = 999L;

    Cart cart = Cart.builder().userId(userId).build();
    cart.addItem(CartItem.builder().productId(10L).quantity(1).build());

    given(cartRepository.findByUserId(userId)).willReturn(Optional.of(cart));

    // when & then
    assertThatThrownBy(() -> cartService.changeQuantity(userId, cartItemId, 3))
        .isInstanceOf(BusinessException.class)
        .extracting("code")
        .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
  }
}
