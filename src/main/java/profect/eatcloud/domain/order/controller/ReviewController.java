package profect.eatcloud.domain.order.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import profect.eatcloud.domain.order.dto.request.ReviewRequestDto;
import profect.eatcloud.domain.order.dto.response.ReviewResponseDto;
import profect.eatcloud.domain.order.service.ReviewService;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "8-2. Review", description = "리뷰 관리 API")
public class ReviewController {

	private final ReviewService reviewService;

	@Operation(summary = "1. 리뷰 생성", description = "주문에 대한 리뷰를 작성합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
	})
	@PostMapping
	public ResponseEntity<ReviewResponseDto> createReview(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody ReviewRequestDto request) {
		UUID customerId = getCustomerUuid(userDetails);
		ReviewResponseDto response = reviewService.createReview(customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "2. 리뷰 목록 조회", description = "사용자의 모든 리뷰를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@GetMapping
	public ResponseEntity<List<ReviewResponseDto>> getReviewList(
		@AuthenticationPrincipal UserDetails userDetails) {
		UUID customerId = getCustomerUuid(userDetails);
		List<ReviewResponseDto> reviews = reviewService.getReviewListByCustomer(customerId);
		return ResponseEntity.ok(reviews);
	}

	@Operation(summary = "3. 리뷰 상세 조회", description = "특정 리뷰의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
	})
	@GetMapping("/{reviewId}")
	public ResponseEntity<ReviewResponseDto> getReview(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable UUID reviewId) {
		UUID customerId = getCustomerUuid(userDetails);
		ReviewResponseDto response = reviewService.getReview(customerId, reviewId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "4. 리뷰 삭제", description = "작성한 리뷰를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
	})
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> deleteReview(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable UUID reviewId) {
		UUID customerId = getCustomerUuid(userDetails);
		reviewService.deleteReview(customerId, reviewId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "5. 매장별 리뷰 목록 조회", description = "특정 매장의 리뷰를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
	})
	@GetMapping("/stores/{storeId}")
	public ResponseEntity<List<ReviewResponseDto>> getReviewsByStore(
		@PathVariable UUID storeId) {
		List<ReviewResponseDto> reviews = reviewService.getReviewListByStore(storeId);
		return ResponseEntity.ok(reviews);
	}

	private UUID getCustomerUuid(UserDetails userDetails) {
		return UUID.fromString(userDetails.getUsername());
	}
}