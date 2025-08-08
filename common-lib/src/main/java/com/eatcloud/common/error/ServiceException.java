package com.eatcloud.common.error;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
  private final ErrorCode errorCode;

  public ServiceException(ErrorCode errorCode) {
    super(errorCode.message());
    this.errorCode = errorCode;
  }

  public ServiceException(ErrorCode errorCode, String detail) {
    super(detail);
    this.errorCode = errorCode;
  }
}


