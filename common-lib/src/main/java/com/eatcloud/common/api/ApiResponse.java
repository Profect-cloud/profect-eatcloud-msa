package com.eatcloud.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ApiResponse<T> {
  private final boolean success;
  private final String code;
  private final String message;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final T data;

  @Builder
  private ApiResponse(boolean success, String code, String message, T data) {
    this.success = success; this.code = code; this.message = message; this.data = data;
  }

  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder().success(true).code("OK").message("success").data(data).build();
  }

  public static <T> ApiResponse<T> failure(String code, String message) {
    return ApiResponse.<T>builder().success(false).code(code).message(message).build();
  }
}


