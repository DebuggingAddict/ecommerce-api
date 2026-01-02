package com.shoppingmall.ecommerceapi.common.infra;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

  // 파일 업로드
  String uploadFile(MultipartFile file);

  // 파일 삭제
  void deleteFile(String fileUrl);
}