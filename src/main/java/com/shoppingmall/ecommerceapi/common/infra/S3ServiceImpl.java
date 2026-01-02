package com.shoppingmall.ecommerceapi.common.infra;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.shoppingmall.ecommerceapi.common.exception.BusinessException;
import com.shoppingmall.ecommerceapi.domain.product.exception.ProductErrorCode;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  // 파일 업로드
  @Override
  public String uploadFile(MultipartFile file) {
    // 파일이 없거나 비어있으면 기본값
    if (file == null || file.isEmpty()) {
      return "none.png";
    }

    // UUID 써서 S3에 저장될 고유한 파일명 만들기
    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentType(file.getContentType()); // 파일 타입

      // S3에 실제 파일 업로드
      amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));

      // 업로드된 파일 전체 URL 주소 반환, 에러 발생할 경우
      return amazonS3.getUrl(bucket, fileName).toString();
    } catch (IOException e) {
      throw new BusinessException(ProductErrorCode.PRODUCT_INVALID_IMAGE);
    }
  }

  // 파일 삭제
  @Override
  public void deleteFile(String fileUrl) {
    // Url 없거나 기본 이미지면 즉시 종료
    if (fileUrl == null || fileUrl.equals("none.png") || fileUrl.isBlank()) {
      return;
    }

    /**
     * 전체 URL에서 실제 파일 이름(key)만 추출 - URL 마지막 / 뒤에 오는게 S3의 객체 키임
     * S3 버킷에서 해당 키 가진 객체 삭제
     */
    try {
      String key = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
      amazonS3.deleteObject(bucket, key);
    } catch (Exception e) {
      System.err.println("S3 파일 삭제 실패: " + e.getMessage());
    }
  }
}