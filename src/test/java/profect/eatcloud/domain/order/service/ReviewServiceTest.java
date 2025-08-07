package profect.eatcloud.domain.order.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.domain.customer.repository.CustomerRepository;
import profect.eatcloud.domain.order.dto.request.ReviewRequestDto;
import profect.eatcloud.domain.order.dto.response.ReviewResponseDto;
import profect.eatcloud.domain.order.entity.Order;
import profect.eatcloud.domain.order.entity.Review;
import profect.eatcloud.domain.order.repository.OrderRepository;
import profect.eatcloud.domain.order.repository.ReviewRepository;
import profect.eatcloud.global.timeData.TimeData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private ReviewService reviewService;

	private final UUID customerId = UUID.randomUUID();
	private final UUID reviewId = UUID.randomUUID();
	private final UUID orderId = UUID.randomUUID();

	@Test
	@DisplayName("리뷰 생성 성공")
	void createReview_Success() {
		// given
		ReviewRequestDto request = new ReviewRequestDto(
			orderId,
			new BigDecimal("4.5"),
			"맛있었어요!"
		);

		Customer customer = Customer.builder()
			.id(customerId)
			.build();

		Order order = Order.builder()
			.orderId(orderId)
			.customerId(customerId)
			.build();

		// TimeData 모킹
		TimeData timeData = mock(TimeData.class);
		given(timeData.getCreatedAt()).willReturn(LocalDateTime.now());

		// Review 모킹 (Builder 대신 Mock 사용)
		Review savedReview = mock(Review.class);
		given(savedReview.getReviewId()).willReturn(reviewId);
		given(savedReview.getOrder()).willReturn(order);
		given(savedReview.getRating()).willReturn(new BigDecimal("4.5"));
		given(savedReview.getContent()).willReturn("맛있었어요!");
		given(savedReview.getTimeData()).willReturn(timeData);

		given(customerRepository.findById(customerId))
			.willReturn(Optional.of(customer));
		given(orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId))
			.willReturn(Optional.of(order));
		given(reviewRepository.existsByOrderOrderIdAndTimeData_DeletedAtIsNull(orderId))
			.willReturn(false);
		given(reviewRepository.save(any(Review.class)))
			.willReturn(savedReview);

		// when
		ReviewResponseDto result = reviewService.createReview(customerId, request);

		// then
		assertThat(result.reviewId()).isEqualTo(reviewId);
		assertThat(result.orderId()).isEqualTo(orderId);
		assertThat(result.rating()).isEqualTo(new BigDecimal("4.5"));
		assertThat(result.content()).isEqualTo("맛있었어요!");
		assertThat(result.createdAt()).isNotNull();
	}

	@Test
	@DisplayName("이미 리뷰가 존재하는 주문에 리뷰 생성 시 예외 발생")
	void createReview_AlreadyExists_ThrowsException() {
		// given
		ReviewRequestDto request = new ReviewRequestDto(
			orderId,
			new BigDecimal("4.5"),
			"맛있었어요!"
		);

		Customer customer = Customer.builder()
			.id(customerId)
			.build();

		Order order = Order.builder()
			.orderId(orderId)
			.customerId(customerId)
			.build();

		given(customerRepository.findById(customerId))
			.willReturn(Optional.of(customer));
		given(orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId))
			.willReturn(Optional.of(order));
		given(reviewRepository.existsByOrderOrderIdAndTimeData_DeletedAtIsNull(orderId))
			.willReturn(true);

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(customerId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("이미 해당 주문에 대한 리뷰가 존재합니다.");
	}

	@Test
	@DisplayName("존재하지 않는 고객으로 리뷰 생성 시 예외 발생")
	void createReview_CustomerNotFound_ThrowsException() {
		// given
		ReviewRequestDto request = new ReviewRequestDto(
			orderId,
			new BigDecimal("4.5"),
			"맛있었어요!"
		);

		given(customerRepository.findById(customerId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(customerId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Customer not found: " + customerId);
	}

	@Test
	@DisplayName("존재하지 않는 주문으로 리뷰 생성 시 예외 발생")
	void createReview_OrderNotFound_ThrowsException() {
		// given
		ReviewRequestDto request = new ReviewRequestDto(
			orderId,
			new BigDecimal("4.5"),
			"맛있었어요!"
		);

		Customer customer = Customer.builder()
			.id(customerId)
			.build();

		given(customerRepository.findById(customerId))
			.willReturn(Optional.of(customer));
		given(orderRepository.findByOrderIdAndCustomerIdAndTimeData_DeletedAtIsNull(orderId, customerId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewService.createReview(customerId, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Order not found or not authorized");
	}

	@Test
	@DisplayName("고객 리뷰 목록 조회 성공")
	void getReviewListByCustomer_Success() {
		// given
		TimeData timeData = mock(TimeData.class);
		given(timeData.getCreatedAt()).willReturn(LocalDateTime.now());

		Order order = mock(Order.class);
		given(order.getOrderId()).willReturn(orderId);

		Review review = mock(Review.class);
		given(review.getReviewId()).willReturn(reviewId);
		given(review.getOrder()).willReturn(order);
		given(review.getRating()).willReturn(new BigDecimal("4.5"));
		given(review.getContent()).willReturn("맛있었어요!");
		given(review.getTimeData()).willReturn(timeData);

		given(reviewRepository.findByOrderCustomerIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(customerId))
			.willReturn(List.of(review));

		// when
		List<ReviewResponseDto> result = reviewService.getReviewListByCustomer(customerId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).reviewId()).isEqualTo(reviewId);
		assertThat(result.get(0).orderId()).isEqualTo(orderId);
		assertThat(result.get(0).rating()).isEqualTo(new BigDecimal("4.5"));
		assertThat(result.get(0).content()).isEqualTo("맛있었어요!");
	}

	@Test
	@DisplayName("매장별 리뷰 목록 조회 성공")
	void getReviewListByStore_Success() {
		// given
		UUID storeId = UUID.randomUUID();
		TimeData timeData = mock(TimeData.class);
		given(timeData.getCreatedAt()).willReturn(LocalDateTime.now());

		Order order = mock(Order.class);
		given(order.getOrderId()).willReturn(orderId);

		Review review = mock(Review.class);
		given(review.getReviewId()).willReturn(reviewId);
		given(review.getOrder()).willReturn(order);
		given(review.getRating()).willReturn(new BigDecimal("5.0"));
		given(review.getContent()).willReturn("정말 맛있어요!");
		given(review.getTimeData()).willReturn(timeData);

		given(reviewRepository.findByOrderStoreIdAndTimeData_DeletedAtIsNullOrderByTimeData_CreatedAtDesc(storeId))
			.willReturn(List.of(review));

		// when
		List<ReviewResponseDto> result = reviewService.getReviewListByStore(storeId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).reviewId()).isEqualTo(reviewId);
		assertThat(result.get(0).rating()).isEqualTo(new BigDecimal("5.0"));
		assertThat(result.get(0).content()).isEqualTo("정말 맛있어요!");
	}

	@Test
	@DisplayName("리뷰 상세 조회 성공")
	void getReview_Success() {
		// given
		TimeData timeData = mock(TimeData.class);
		given(timeData.getCreatedAt()).willReturn(LocalDateTime.now());

		Order order = mock(Order.class);
		given(order.getOrderId()).willReturn(orderId);

		Review review = mock(Review.class);
		given(review.getReviewId()).willReturn(reviewId);
		given(review.getOrder()).willReturn(order);
		given(review.getRating()).willReturn(new BigDecimal("4.5"));
		given(review.getContent()).willReturn("맛있었어요!");
		given(review.getTimeData()).willReturn(timeData);

		given(reviewRepository.findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId))
			.willReturn(Optional.of(review));

		// when
		ReviewResponseDto result = reviewService.getReview(customerId, reviewId);

		// then
		assertThat(result.reviewId()).isEqualTo(reviewId);
		assertThat(result.orderId()).isEqualTo(orderId);
		assertThat(result.rating()).isEqualTo(new BigDecimal("4.5"));
		assertThat(result.content()).isEqualTo("맛있었어요!");
		assertThat(result.createdAt()).isNotNull();
	}

	@Test
	@DisplayName("존재하지 않는 리뷰 조회 시 예외 발생")
	void getReview_NotFound_ThrowsException() {
		// given
		given(reviewRepository.findByReviewIdAndOrderCustomerIdAndTimeData_DeletedAtIsNull(reviewId, customerId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reviewService.getReview(customerId, reviewId))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("리뷰를 찾을 수 없거나 접근 권한이 없습니다.");
	}
}