package com.shoppingmall.ecommerceapi.domain.cart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.ecommerceapi.domain.cart.dto.AddCartItemRequest;
import com.shoppingmall.ecommerceapi.domain.cart.dto.CartResponse;
import com.shoppingmall.ecommerceapi.domain.cart.dto.ChangeCartItemQuantityRequest;
import com.shoppingmall.ecommerceapi.domain.cart.entity.Cart;
import com.shoppingmall.ecommerceapi.domain.cart.entity.CartItem;
import com.shoppingmall.ecommerceapi.domain.cart.service.CartService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  CartService cartService;

  private static final String BASE_URL = "/api/carts";
  private static final String USER_HEADER = "X-USER-ID";

  @Test
  @DisplayName("GET" + BASE_URL + "- 장바구니 조회 성공")
  void getCart_success() throws Exception {
    // given
    Long userId = 1L;
    CartResponse response = CartResponse.builder()
        .cartId(10L)
        .userId(userId)
        .updatedAt(LocalDateTime.now())
        .items(List.of())
        .build();

    given(cartService.getCartResponse(userId)).willReturn(response);

    // when & then
    mockMvc.perform(get(BASE_URL)
            .header(USER_HEADER, userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").exists())
        .andExpect(jsonPath("$.body.cartId").value(10L))
        .andExpect(jsonPath("$.body.userId").value(1L));
  }

  @Test
  @DisplayName("GET" + BASE_URL + "- 헤더 없으면 400")
  void getCart_missingHeader_400() throws Exception {
    mockMvc.perform(get(BASE_URL))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST" + BASE_URL + "/items - 장바구니 담기 성공")
  void addItem_success() throws Exception {
    // given
    Long userId = 1L;

    AddCartItemRequest request = AddCartItemRequest.builder()
        .productId(100L)
        .quantity(2)
        .build();

    Cart cart = Cart.builder().userId(userId).build();
    // cartId는 CartConverter 응답에 필요하므로 값이 있다고 가정(테스트에서는 리플렉션 대신 단순 검증만)
    // 실제 응답 검증을 cartId까지 하려면, Cart 엔티티에 id 세팅 가능한 구조인지에 따라 조정 필요.

    CartItem item = CartItem.builder()
        .productId(100L)
        .quantity(2)
        .build();

    given(cartService.getCartByUserId(userId)).willReturn(cart);
    given(cartService.addItem(eq(userId), any(AddCartItemRequest.class))).willReturn(item);

    // when & then
    mockMvc.perform(post(BASE_URL + "/items")
            .header(USER_HEADER, userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").exists())
        .andExpect(jsonPath("$.body.productId").value(100L))
        .andExpect(jsonPath("$.body.productQuantity").value(2));
  }

  @Test
  @DisplayName("POST" + BASE_URL + "/items - quantity가 0이면 @Valid로 400")
  void addItem_invalidBody_400() throws Exception {
    // given
    Long userId = 1L;
    AddCartItemRequest request = AddCartItemRequest.builder()
        .productId(100L)
        .quantity(0) // @Min(1)
        .build();

    // when & then
    mockMvc.perform(post(BASE_URL + "/items")
            .header(USER_HEADER, userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PATCH" + BASE_URL + "/items/{id} - 수량 변경 성공")
  void changeQuantity_success() throws Exception {
    // given
    Long userId = 1L;
    Long cartItemId = 5L;

    ChangeCartItemQuantityRequest request = ChangeCartItemQuantityRequest.builder()
        .quantity(3)
        .build();

    CartItem updated = CartItem.builder()
        .productId(200L)
        .quantity(3)
        .build();

    given(cartService.changeQuantity(userId, cartItemId, 3)).willReturn(updated);

    // when & then
    mockMvc.perform(patch(BASE_URL + "/items/{id}", cartItemId)
            .header(USER_HEADER, userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").exists())
        .andExpect(jsonPath("$.body.productId").value(200L))
        .andExpect(jsonPath("$.body.productQuantity").value(3));
  }

  @Test
  @DisplayName("PATCH " + BASE_URL + "/items/{id} - quantity가 0이면 @Valid로 400")
  void changeQuantity_invalidBody_400() throws Exception {
    // given
    Long userId = 1L;
    Long cartItemId = 5L;

    ChangeCartItemQuantityRequest request = ChangeCartItemQuantityRequest.builder()
        .quantity(0) // @Min(1)
        .build();

    // when & then
    mockMvc.perform(patch(BASE_URL + "/items/{id}", cartItemId)
            .header(USER_HEADER, userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("DELETE " + BASE_URL + "/items/{productId} - 특정 상품 삭제 성공")
  void removeItem_success() throws Exception {
    // given
    Long userId = 1L;
    Long productId = 100L;

    willDoNothing().given(cartService).removeItem(userId, productId);

    // when & then
    mockMvc.perform(delete(BASE_URL + "/items/{productId}", productId)
            .header(USER_HEADER, userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").exists())
        .andExpect(jsonPath("$.body").doesNotExist());
  }

  @Test
  @DisplayName("DELETE " + BASE_URL + "/items - 장바구니 비우기 성공")
  void clearCart_success() throws Exception {
    // given
    Long userId = 1L;

    willDoNothing().given(cartService).clearCart(userId);

    // when & then
    mockMvc.perform(delete(BASE_URL + "/items")
            .header(USER_HEADER, userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").exists())
        .andExpect(jsonPath("$.body").doesNotExist());
  }
}
