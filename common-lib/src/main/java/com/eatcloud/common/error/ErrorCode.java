package com.eatcloud.common.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
  HttpStatus httpStatus();
  String code();
  String message();
}


