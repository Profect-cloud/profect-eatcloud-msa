package profect.eatcloud.domain.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponseDto(
	UUID reviewId,
	UUID orderId,
	BigDecimal rating,
	String content,
	LocalDateTime createdAt
) {}