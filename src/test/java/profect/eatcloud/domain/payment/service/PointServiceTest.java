package profect.eatcloud.domain.payment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.domain.customer.repository.CustomerRepository;
import profect.eatcloud.domain.payment.service.PointService.PointResult;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PointService pointService;

    @DisplayName("포인트 충분 시 사용 성공")
    @Test
    void givenSufficientPoints_whenUsePoints_thenReturnSuccess() {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(customerId)
                .points(5000)
                .build();

        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(customer));
        given(customerRepository.save(any(Customer.class)))
                .willReturn(customer);

        PointResult result = pointService.usePoints(customerId, 2000);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getUsedPoints()).isEqualTo(2000);
        assertThat(result.getRemainingPoints()).isEqualTo(3000);
        assertThat(customer.getPoints()).isEqualTo(3000);
        then(customerRepository).should().save(customer);
    }

    @DisplayName("포인트 부족 시 사용 실패")
    @Test
    void givenInsufficientPoints_whenUsePoints_thenReturnFailure() {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(customerId)
                .points(1000)
                .build();

        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(customer));

        PointResult result = pointService.usePoints(customerId, 2000);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("포인트가 부족합니다");
        assertThat(customer.getPoints()).isEqualTo(1000); // 원래 포인트 유지
        then(customerRepository).should(never()).save(any(Customer.class));
    }

    @DisplayName("존재하지 않는 고객 포인트 사용 실패")
    @Test
    void givenNonExistentCustomer_whenUsePoints_thenReturnFailure() {
        UUID nonExistentCustomerId = UUID.randomUUID();

        given(customerRepository.findById(nonExistentCustomerId))
                .willReturn(Optional.empty());

        PointResult result = pointService.usePoints(nonExistentCustomerId, 1000);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("고객을 찾을 수 없습니다");
    }

    @DisplayName("0 포인트 사용 시 실패")
    @Test
    void givenZeroPoints_whenUsePoints_thenReturnFailure() {
        UUID customerId = UUID.randomUUID();

        PointResult result = pointService.usePoints(customerId, 0);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("유효하지 않은 포인트 사용 요청입니다");
        then(customerRepository).should(never()).findById(any(UUID.class));
        then(customerRepository).should(never()).save(any(Customer.class));
    }

    @DisplayName("음수 포인트 사용 시 실패")
    @Test
    void givenNegativePoints_whenUsePoints_thenReturnFailure() {
        UUID customerId = UUID.randomUUID();

        PointResult result = pointService.usePoints(customerId, -1000);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("유효하지 않은 포인트 사용 요청입니다");
        then(customerRepository).should(never()).findById(any(UUID.class));
        then(customerRepository).should(never()).save(any(Customer.class));
    }

    @DisplayName("null 포인트 사용 시 실패")
    @Test
    void givenNullPoints_whenUsePoints_thenReturnFailure() {
        UUID customerId = UUID.randomUUID();

        PointResult result = pointService.usePoints(customerId, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("유효하지 않은 포인트 사용 요청입니다");
        then(customerRepository).should(never()).findById(any(UUID.class));
        then(customerRepository).should(never()).save(any(Customer.class));
    }

    @DisplayName("null 고객 ID로 포인트 사용 시 실패")
    @Test
    void givenNullCustomerId_whenUsePoints_thenReturnFailure() {
        Integer pointsToUse = 1000;

        PointResult result = pointService.usePoints(null, pointsToUse);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("유효하지 않은 포인트 사용 요청입니다");
        then(customerRepository).should(never()).findById(any(UUID.class));
        then(customerRepository).should(never()).save(any(Customer.class));
    }

    @DisplayName("포인트 적립 성공")
    @Test
    void givenValidCustomer_whenRefundPoints_thenReturnSuccess() {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(customerId)
                .points(1000)
                .build();

        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(customer));
        given(customerRepository.save(any(Customer.class)))
                .willReturn(customer);

        PointResult result = pointService.refundPoints(customerId, 500);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getUsedPoints()).isEqualTo(500);
        assertThat(result.getRemainingPoints()).isEqualTo(1500);
        assertThat(customer.getPoints()).isEqualTo(1500);
        then(customerRepository).should().save(customer);
    }

    @DisplayName("포인트 사용 가능 여부 확인 - 충분한 포인트")
    @Test
    void givenSufficientPoints_whenCanUsePoints_thenReturnTrue() {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(customerId)
                .points(3000)
                .build();

        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(customer));

        boolean canUse = pointService.canUsePoints(customerId, 2000);

        assertThat(canUse).isTrue();
    }

    @DisplayName("포인트 사용 가능 여부 확인 - 부족한 포인트")
    @Test
    void givenInsufficientPoints_whenCanUsePoints_thenReturnFalse() {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(customerId)
                .points(1000)
                .build();

        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(customer));

        boolean canUse = pointService.canUsePoints(customerId, 2000);

        assertThat(canUse).isFalse();
    }

    @DisplayName("null 값으로 포인트 사용 가능 여부 확인 시 false 반환")
    @Test
    void givenNullValues_whenCanUsePoints_thenReturnFalse() {
        assertThat(pointService.canUsePoints(null, 1000)).isFalse();
        assertThat(pointService.canUsePoints(UUID.randomUUID(), null)).isFalse();
        assertThat(pointService.canUsePoints(null, null)).isFalse();
        assertThat(pointService.canUsePoints(UUID.randomUUID(), 0)).isFalse();
        assertThat(pointService.canUsePoints(UUID.randomUUID(), -1000)).isFalse();
    }
}