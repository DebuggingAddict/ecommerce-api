package com.shoppingmall.ecommerceapi.domain.order.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class OrderNumberGenerator {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  /**
   * 주문번호 생성 형식: yyyyMMdd + 6자리 일련번호 예시: 20251225000001
   *
   * @param sequenceNumber 오늘 날짜의 몇 번째 주문인지
   * @return 생성된 주문번호
   */
  public String generate(Long sequenceNumber) {
    String datePrefix = LocalDate.now().format(DATE_FORMATTER);
    String sequencePart = String.format("%06d", sequenceNumber);
    return datePrefix + sequencePart;
  }

  /**
   * 오늘 날짜의 prefix 반환 예시: 20251225
   */
  public String getTodayPrefix() {
    return LocalDate.now().format(DATE_FORMATTER);
  }
}
