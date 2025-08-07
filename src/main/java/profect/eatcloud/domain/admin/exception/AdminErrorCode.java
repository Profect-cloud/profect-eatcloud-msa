package profect.eatcloud.domain.admin.exception;

public enum AdminErrorCode {
	ADMIN_NOT_FOUND("ADMIN_001", "해당 관리자를 찾을 수 없습니다"),
	EMAIL_ALREADY_EXISTS("ADMIN_002", "이미 사용 중인 이메일입니다"),
	INVALID_INPUT("ADMIN_003", "잘못된 입력값입니다"),
	STORE_NOT_FOUND("ADMIN_004", "해당 매장이 존재하지 않습니다"),
	CATEGORY_NOT_FOUND("ADMIN_005", "해당 카테고리가 존재하지 않습니다"),
	CUSTOMER_NOT_FOUND("ADMIN_006", "해당 고객을 찾을 수 없습니다"),
	MANAGER_NOT_FOUND("ADMIN_007", "해당 매니저를 찾을 수 없습니다"),
	APPLICATION_NOT_FOUND("ADMIN_008", "해당 신청서를 찾을 수 없습니다"),
	APPLICATION_ALREADY_PROCESSED("ADMIN_009", "이미 처리된 신청서입니다"),
	APPLICATION_EMAIL_ALREADY_EXISTS("ADMIN_010", "이미 해당 이메일로 신청된 이력이 있습니다");

	private final String code;
	private final String message;

	AdminErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}

