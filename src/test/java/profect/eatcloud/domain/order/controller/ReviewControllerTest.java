package profect.eatcloud.domain.order.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import profect.eatcloud.domain.order.dto.request.ReviewRequestDto;
import profect.eatcloud.domain.order.dto.response.ReviewResponseDto;
import profect.eatcloud.domain.order.service.ReviewService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

	@Mock
	private ReviewService reviewService;

	private ReviewController reviewController;
	private UUID customerId;
	private UserDetails userDetails;

	@BeforeEach
	void setUp() {
		reviewController = new ReviewController(reviewService);
		customerId = UUID.randomUUID();

		// UserDetails 모킹 - username에 customerId를 String으로 변환하여 저장
		userDetails = User.builder()
			.username(customerId.toString())
			.password("password")
			.authorities(Collections.emptyList())
			.build();
	}

	@Test
	@DisplayName("리뷰 생성 - 성공")
	void createReview_Success() {
		// given
		UUID orderId = UUID.randomUUID();
		ReviewRequestDto request = new ReviewRequestDto(
			orderId,
			new BigDecimal("4.5"),
			"맛있었어요!"
		);

		UUID reviewId = UUID.randomUUID();
		ReviewResponseDto expectedResponse = new ReviewResponseDto(
			reviewId,
			orderId,
			new BigDecimal("4.5"),
			"맛있었어요!",
			LocalDateTime.now()
		);

		given(reviewService.createReview(eq(customerId), any(ReviewRequestDto.class)))
			.willReturn(expectedResponse);

		// when
		ResponseEntity<ReviewResponseDto> response =
			reviewController.createReview(userDetails, request);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().reviewId()).isEqualTo(reviewId);
		assertThat(response.getBody().orderId()).isEqualTo(orderId);
		assertThat(response.getBody().rating()).isEqualTo(new BigDecimal("4.5"));
		assertThat(response.getBody().content()).isEqualTo("맛있었어요!");
	}

	@Test
	@DisplayName("리뷰 생성 - 이미 리뷰가 존재하는 경우")
	void createReview_ReviewAlreadyExists() {
		// given
		UUID orderId = UUID.randomUUID();
		ReviewRequestDto request = new ReviewRequestDto(
			orderId,
			new BigDecimal("4.5"),
			"맛있었어요!"
		);

		given(reviewService.createReview(eq(customerId), any(ReviewRequestDto.class)))
			.willThrow(new IllegalArgumentException("이미 해당 주문에 대한 리뷰가 존재합니다."));

		// when & then
		assertThatThrownBy(() -> reviewController.createReview(userDetails, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("이미 해당 주문에 대한 리뷰가 존재합니다.");
	}

	@Test
	@DisplayName("리뷰 생성 - 존재하지 않는 주문")
	void createReview_OrderNotFound() {
		// given
		UUID orderId = UUID.randomUUID();
		ReviewRequestDto request = new ReviewRequestDto(
			orderId,
			new BigDecimal("4.5"),
			"맛있었어요!"
		);

		given(reviewService.createReview(eq(customerId), any(ReviewRequestDto.class)))
			.willThrow(new IllegalArgumentException("Order not found or not authorized"));

		// when & then
		assertThatThrownBy(() -> reviewController.createReview(userDetails, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Order not found or not authorized");
	}

	@Test
	@DisplayName("리뷰 목록 조회 - 성공")
	void getReviewList_Success() {
		// given
		UUID reviewId1 = UUID.randomUUID();
		UUID reviewId2 = UUID.randomUUID();
		UUID orderId1 = UUID.randomUUID();
		UUID orderId2 = UUID.randomUUID();

		ReviewResponseDto review1 = new ReviewResponseDto(
			reviewId1,
			orderId1,
			new BigDecimal("4.5"),
			"맛있었어요!",
			LocalDateTime.now()
		);
		ReviewResponseDto review2 = new ReviewResponseDto(
			reviewId2,
			orderId2,
			new BigDecimal("3.0"),
			"괜찮았어요.",
			LocalDateTime.now().minusDays(1)
		);
		List<ReviewResponseDto> reviews = Arrays.asList(review1, review2);

		given(reviewService.getReviewListByCustomer(customerId)).willReturn(reviews);

		// when
		ResponseEntity<List<ReviewResponseDto>> response =
			reviewController.getReviewList(userDetails);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody().get(0).reviewId()).isEqualTo(reviewId1);
		assertThat(response.getBody().get(0).rating()).isEqualTo(new BigDecimal("4.5"));
		assertThat(response.getBody().get(1).reviewId()).isEqualTo(reviewId2);
		assertThat(response.getBody().get(1).rating()).isEqualTo(new BigDecimal("3.0"));
	}

	@Test
	@DisplayName("리뷰 목록 조회 - 빈 목록")
	void getReviewList_EmptyList() {
		// given
		given(reviewService.getReviewListByCustomer(customerId)).willReturn(List.of());

		// when
		ResponseEntity<List<ReviewResponseDto>> response =
			reviewController.getReviewList(userDetails);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEmpty();
	}

	@Test
	@DisplayName("리뷰 상세 조회 - 성공")
	void getReview_Success() {
		// given
		UUID reviewId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		ReviewResponseDto expectedResponse = new ReviewResponseDto(
			reviewId,
			orderId,
			new BigDecimal("4.5"),
			"맛있었어요!",
			LocalDateTime.now()
		);

		given(reviewService.getReview(eq(customerId), eq(reviewId)))
			.willReturn(expectedResponse);

		// when
		ResponseEntity<ReviewResponseDto> response =
			reviewController.getReview(userDetails, reviewId);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().reviewId()).isEqualTo(reviewId);
		assertThat(response.getBody().orderId()).isEqualTo(orderId);
		assertThat(response.getBody().rating()).isEqualTo(new BigDecimal("4.5"));
		assertThat(response.getBody().content()).isEqualTo("맛있었어요!");
	}

	@Test
	@DisplayName("리뷰 상세 조회 - 존재하지 않는 리뷰")
	void getReview_ReviewNotFound() {
		// given
		UUID reviewId = UUID.randomUUID();

		given(reviewService.getReview(eq(customerId), eq(reviewId)))
			.willThrow(new IllegalArgumentException("리뷰를 찾을 수 없거나 접근 권한이 없습니다."));

		// when & then
		assertThatThrownBy(() -> reviewController.getReview(userDetails, reviewId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("리뷰를 찾을 수 없거나 접근 권한이 없습니다.");
	}

	@Test
	@DisplayName("리뷰 삭제 - 성공")
	void deleteReview_Success() {
		// given
		UUID reviewId = UUID.randomUUID();
		willDoNothing().given(reviewService).deleteReview(customerId, reviewId);

		// when
		ResponseEntity<Void> response = reviewController.deleteReview(userDetails, reviewId);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
	}

	@Test
	@DisplayName("리뷰 삭제 - 존재하지 않는 리뷰")
	void deleteReview_ReviewNotFound() {
		// given
		UUID reviewId = UUID.randomUUID();
		willThrow(new IllegalArgumentException("리뷰를 찾을 수 없거나 접근 권한이 없습니다."))
			.given(reviewService).deleteReview(customerId, reviewId);

		// when & then
		assertThatThrownBy(() -> reviewController.deleteReview(userDetails, reviewId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("리뷰를 찾을 수 없거나 접근 권한이 없습니다.");
	}

	@Test
	@DisplayName("매장별 리뷰 목록 조회 - 성공")
	void getReviewsByStore_Success() {
		// given
		UUID storeId = UUID.randomUUID();
		UUID reviewId1 = UUID.randomUUID();
		UUID reviewId2 = UUID.randomUUID();
		UUID orderId1 = UUID.randomUUID();
		UUID orderId2 = UUID.randomUUID();

		ReviewResponseDto review1 = new ReviewResponseDto(
			reviewId1,
			orderId1,
			new BigDecimal("5.0"),
			"정말 맛있어요!",
			LocalDateTime.now()
		);
		ReviewResponseDto review2 = new ReviewResponseDto(
			reviewId2,
			orderId2,
			new BigDecimal("4.0"),
			"좋았습니다.",
			LocalDateTime.now().minusHours(2)
		);
		List<ReviewResponseDto> reviews = Arrays.asList(review1, review2);

		given(reviewService.getReviewListByStore(storeId)).willReturn(reviews);

		// when
		ResponseEntity<List<ReviewResponseDto>> response =
			reviewController.getReviewsByStore(storeId);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody().get(0).reviewId()).isEqualTo(reviewId1);
		assertThat(response.getBody().get(0).rating()).isEqualTo(new BigDecimal("5.0"));
		assertThat(response.getBody().get(1).reviewId()).isEqualTo(reviewId2);
		assertThat(response.getBody().get(1).rating()).isEqualTo(new BigDecimal("4.0"));
	}

	@Test
	@DisplayName("매장별 리뷰 목록 조회 - 빈 목록")
	void getReviewsByStore_EmptyList() {
		// given
		UUID storeId = UUID.randomUUID();
		given(reviewService.getReviewListByStore(storeId)).willReturn(List.of());

		// when
		ResponseEntity<List<ReviewResponseDto>> response =
			reviewController.getReviewsByStore(storeId);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEmpty();
	}
}