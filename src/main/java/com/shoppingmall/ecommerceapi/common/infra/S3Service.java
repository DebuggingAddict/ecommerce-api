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
public class S3Service {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public String uploadFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return "none.png";
    }

    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(file.getSize());
      metadata.setContentType(file.getContentType());

      amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));

      return amazonS3.getUrl(bucket, fileName).toString();
    } catch (IOException e) {
      throw new BusinessException(ProductErrorCode.PRODUCT_INVALID_IMAGE);
    }
  }
}