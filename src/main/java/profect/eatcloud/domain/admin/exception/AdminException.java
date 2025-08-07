package profect.eatcloud.domain.admin.exception;

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