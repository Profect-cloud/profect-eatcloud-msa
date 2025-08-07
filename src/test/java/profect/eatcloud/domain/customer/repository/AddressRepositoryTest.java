package profect.eatcloud.domain.customer.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import profect.eatcloud.domain.customer.entity.Address;
import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.global.timeData.JpaConfig;
import profect.eatcloud.global.timeData.TimeData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)  // JpaConfig 추가하여 BaseTimeRepositoryImpl 로드
@TestPropertySource(properties = {
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
class AddressRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private CustomerRepository customerRepository;

	private Customer customer;
	private TimeData timeData;

	@BeforeEach
	void setUp() {
		// TimeData 설정
		timeData = TimeData.builder()
			.pTimeId(UUID.randomUUID())
			.createdAt(LocalDateTime.now())
			.createdBy("test")
			.updatedAt(LocalDateTime.now())
			.updatedBy("test")
			.build();

		// TimeData를 먼저 저장
		entityManager.persistAndFlush(timeData);

		// 테스트용 Customer 생성
		customer = Customer.builder()
			.name("testUser")
			.nickname("테스트유저")
			.email("test@example.com")
			.password("password123")
			.phoneNumber("010-1234-5678")
			.build();
		customer.setTimeData(timeData);

		customer = customerRepository.save(customer);
		entityManager.flush();
	}

	@Test
	@DisplayName("고객 ID로 삭제되지 않은 주소 목록 조회")
	void findByCustomerIdAndTimeData_DeletedAtIsNull() {
		// given
		Address address1 = createAddressWithTimeData("12345", "서울시 강남구 테스트로 123", "101호", true);
		Address address2 = createAddressWithTimeData("67890", "서울시 서초구 샘플로 456", "202호", false);

		entityManager.persistAndFlush(address1.getTimeData());
		entityManager.persistAndFlush(address2.getTimeData());

		addressRepository.save(address1);
		addressRepository.save(address2);
		entityManager.flush();

		// when
		List<Address> addresses = addressRepository.findByCustomerIdAndTimeData_DeletedAtIsNull(customer.getId());

		// then
		assertThat(addresses).hasSize(2);
		assertThat(addresses).extracting("zipcode").containsExactlyInAnyOrder("12345", "67890");
	}

	@Test
	@DisplayName("고객의 선택된 주소 조회")
	void findByCustomerIdAndIsSelectedTrue() {
		// given
		Address selectedAddress = createAddressWithTimeData("12345", "서울시 강남구 테스트로 123", "101호", true);
		Address normalAddress = createAddressWithTimeData("67890", "서울시 서초구 샘플로 456", "202호", false);

		entityManager.persistAndFlush(selectedAddress.getTimeData());
		entityManager.persistAndFlush(normalAddress.getTimeData());

		addressRepository.save(selectedAddress);
		addressRepository.save(normalAddress);
		entityManager.flush();

		// when
		Optional<Address> result = addressRepository.findByCustomerIdAndIsSelectedTrueAndTimeData_DeletedAtIsNull(customer.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getZipcode()).isEqualTo("12345");
		assertThat(result.get().getIsSelected()).isTrue();
	}

	@Test
	@DisplayName("주소 ID와 고객 ID로 주소 조회")
	void findByIdAndCustomerId() {
		// given
		Address address = createAddressWithTimeData("12345", "서울시 강남구 테스트로 123", "101호", false);
		entityManager.persistAndFlush(address.getTimeData());
		Address savedAddress = addressRepository.save(address);
		entityManager.flush();

		// when
		Optional<Address> result = addressRepository.findByIdAndCustomerId(savedAddress.getId(), customer.getId());

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getZipcode()).isEqualTo("12345");
		assertThat(result.get().getCustomer().getId()).isEqualTo(customer.getId());
	}

	@Test
	@DisplayName("삭제된 주소는 조회되지 않음")
	void deletedAddressNotFound() {
		// given
		TimeData deletedTimeData = TimeData.builder()
			.pTimeId(UUID.randomUUID())
			.createdAt(LocalDateTime.now())
			.createdBy("test")
			.updatedAt(LocalDateTime.now())
			.updatedBy("test")
			.deletedAt(LocalDateTime.now()) // 삭제됨
			.deletedBy("test")
			.build();

		entityManager.persistAndFlush(deletedTimeData);

		Address deletedAddress = Address.builder()
			.zipcode("12345")
			.roadAddr("서울시 강남구 테스트로 123")
			.detailAddr("101호")
			.isSelected(false)
			.customer(customer)
			.build();
		deletedAddress.setTimeData(deletedTimeData);

		addressRepository.save(deletedAddress);
		entityManager.flush();

		// when
		List<Address> addresses = addressRepository.findByCustomerIdAndTimeData_DeletedAtIsNull(customer.getId());

		// then
		assertThat(addresses).isEmpty();
	}

	@Test
	@DisplayName("다른 고객의 주소는 조회되지 않음")
	void notFoundOtherCustomerAddress() {
		// given
		// 다른 고객 생성
		TimeData otherTimeData = TimeData.builder()
			.pTimeId(UUID.randomUUID())
			.createdAt(LocalDateTime.now())
			.createdBy("test")
			.updatedAt(LocalDateTime.now())
			.updatedBy("test")
			.build();

		entityManager.persistAndFlush(otherTimeData);

		Customer otherCustomer = Customer.builder()
			.name("otherUser")
			.nickname("다른유저")
			.email("other@example.com")
			.password("password123")
			.phoneNumber("010-9999-9999")
			.build();
		otherCustomer.setTimeData(otherTimeData);
		otherCustomer = customerRepository.save(otherCustomer);

		// 다른 고객의 주소
		Address otherAddress = createAddressForCustomer(otherCustomer, "99999", "서울시 종로구", "999호", false);
		entityManager.persistAndFlush(otherAddress.getTimeData());
		addressRepository.save(otherAddress);

		// 현재 고객의 주소
		Address myAddress = createAddressWithTimeData("12345", "서울시 강남구", "101호", false);
		entityManager.persistAndFlush(myAddress.getTimeData());
		addressRepository.save(myAddress);
		entityManager.flush();

		// when
		List<Address> addresses = addressRepository.findByCustomerIdAndTimeData_DeletedAtIsNull(customer.getId());

		// then
		assertThat(addresses).hasSize(1);
		assertThat(addresses.get(0).getZipcode()).isEqualTo("12345");
	}

	@Test
	@DisplayName("soft delete 기능 테스트")
	void softDeleteByTimeId() {
		// given
		Address address = createAddressWithTimeData("12345", "서울시 강남구", "101호", false);
		entityManager.persistAndFlush(address.getTimeData());
		Address savedAddress = addressRepository.save(address);
		entityManager.flush();
		entityManager.clear();

		UUID timeId = savedAddress.getTimeData().getPTimeId();

		// when
		addressRepository.softDeleteByTimeId(timeId, LocalDateTime.now(), "testUser");
		entityManager.flush();
		entityManager.clear();

		// then
		List<Address> activeAddresses = addressRepository.findByCustomerIdAndTimeData_DeletedAtIsNull(customer.getId());
		assertThat(activeAddresses).isEmpty();

		// 삭제된 주소 확인 (BaseTimeRepository의 findDeleted 메서드 사용)
		List<Address> deletedAddresses = addressRepository.findDeleted();
		assertThat(deletedAddresses).isNotEmpty();
		assertThat(deletedAddresses.getFirst().getTimeData().getDeletedAt()).isNotNull();
		assertThat(deletedAddresses.getFirst().getTimeData().getDeletedBy()).isEqualTo("testUser");
	}

	@Test
	@DisplayName("선택된 주소가 없는 경우 빈 Optional 반환")
	void findSelectedAddress_WhenNoSelected() {
		// given
		Address address1 = createAddressWithTimeData("12345", "서울시 강남구", "101호", false);
		Address address2 = createAddressWithTimeData("67890", "서울시 서초구", "202호", false);

		entityManager.persistAndFlush(address1.getTimeData());
		entityManager.persistAndFlush(address2.getTimeData());

		addressRepository.save(address1);
		addressRepository.save(address2);
		entityManager.flush();

		// when
		Optional<Address> result = addressRepository.findByCustomerIdAndIsSelectedTrueAndTimeData_DeletedAtIsNull(customer.getId());

		// then
		assertThat(result).isEmpty();
	}

	private Address createAddressWithTimeData(String zipcode, String roadAddr, String detailAddr, boolean isSelected) {
		TimeData td = TimeData.builder()
			.pTimeId(UUID.randomUUID())
			.createdAt(LocalDateTime.now())
			.createdBy("test")
			.updatedAt(LocalDateTime.now())
			.updatedBy("test")
			.build();

		Address address = Address.builder()
			.zipcode(zipcode)
			.roadAddr(roadAddr)
			.detailAddr(detailAddr)
			.isSelected(isSelected)
			.customer(customer)
			.build();
		address.setTimeData(td);

		return address;
	}

	private Address createAddressForCustomer(Customer customer, String zipcode, String roadAddr, String detailAddr, boolean isSelected) {
		TimeData td = TimeData.builder()
			.pTimeId(UUID.randomUUID())
			.createdAt(LocalDateTime.now())
			.createdBy("test")
			.updatedAt(LocalDateTime.now())
			.updatedBy("test")
			.build();

		Address address = Address.builder()
			.zipcode(zipcode)
			.roadAddr(roadAddr)
			.detailAddr(detailAddr)
			.isSelected(isSelected)
			.customer(customer)
			.build();
		address.setTimeData(td);

		return address;
	}
}