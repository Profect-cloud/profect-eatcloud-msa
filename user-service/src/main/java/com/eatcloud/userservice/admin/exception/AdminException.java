package com.eatcloud.userservice.admin.exception;

import com.eatcloud.userservice.admin.exception.AdminErrorCode;

public class AdminException extends RuntimeException {
	private final AdminErrorCode errorCode;

	public AdminException(AdminErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public AdminErrorCode getErrorCode() {
		return errorCode;
	}
}