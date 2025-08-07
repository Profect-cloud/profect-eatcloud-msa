package profect.eatcloud.domain.customer.exception;

public class CustomerException extends RuntimeException {
	private final CustomerErrorCode errorCode;

	public CustomerException(CustomerErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public CustomerErrorCode getErrorCode() {
		return errorCode;
	}
}