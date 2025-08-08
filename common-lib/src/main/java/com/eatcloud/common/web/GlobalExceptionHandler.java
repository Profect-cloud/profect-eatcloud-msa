package com.eatcloud.common.web;

import com.eatcloud.common.api.ApiResponse;
import com.eatcloud.common.error.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ServiceException.class)
  public ResponseEntity<ApiResponse<Void>> handleServiceException(ServiceException ex) {
    var ec = ex.getErrorCode();
    return ResponseEntity.status(ec.httpStatus())
        .body(ApiResponse.failure(ec.code(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    String msg = ex.getBindingResult().getAllErrors().isEmpty()
        ? "유효성 검증 실패" : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.failure("VALIDATION_ERROR", msg));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.failure("REQUEST_BODY_ERROR", "요청 본문을 읽을 수 없습니다"));
  }

  @ExceptionHandler(RestClientResponseException.class)
  public ResponseEntity<ApiResponse<Void>> handleUpstream(RestClientResponseException ex) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(ApiResponse.failure("UPSTREAM_ERROR", "외부 호출 실패"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.failure("INTERNAL_ERROR", "예기치 못한 오류가 발생했습니다"));
  }
}


