package profect.eatcloud.login.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import profect.eatcloud.domain.admin.entity.Admin;
import profect.eatcloud.domain.admin.repository.AdminRepository;
import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.domain.customer.repository.CustomerRepository;
import profect.eatcloud.domain.manager.entity.Manager;
import profect.eatcloud.domain.manager.repository.ManagerRepository;
import profect.eatcloud.login.dto.LoginResponseDto;
import profect.eatcloud.login.dto.SignupRedisData;
import profect.eatcloud.login.dto.SignupRequestDto;
import profect.eatcloud.security.jwt.JwtTokenProvider;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private CustomerRepository customerRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private ManagerRepository managerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private MailService mailService;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;

    @InjectMocks private AuthService authService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    @Test
    void tempSignup_WhenValidRequest_SendsEmailAndStoresDataInRedis() {
        // given
        SignupRequestDto req = new SignupRequestDto("test@example.com", "Password1!", "홍길동", "길동이", "010-1234-1234");

        given(customerRepository.findByEmail(req.getEmail())).willReturn(Optional.empty());
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        authService.tempSignup(req);

        // then
        verify(mailService).sendMail(eq(req.getEmail()), anyString(), contains("회원가입 인증 코드:"));
        verify(valueOperations).set(
                eq("signup:" + req.getEmail()),
                any(SignupRedisData.class),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void confirmEmail_WhenValidCode_RegistersCustomerAndDeletesRedis() {
        // given
        SignupRequestDto req = new SignupRequestDto("test@example.com", "Password1!", "홍길동", "길동이", "010-1234-1234");
        String code = "123456";
        SignupRedisData data = new SignupRedisData(req, code);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("signup:" + req.getEmail())).willReturn(data);
        given(passwordEncoder.encode(req.getPassword())).willReturn("encodedPassword");

        // when
        authService.confirmEmail(req.getEmail(), code);

        // then
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertThat(savedCustomer.getEmail()).isEqualTo(req.getEmail());
        assertThat(savedCustomer.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedCustomer.getName()).isEqualTo(req.getName());
        assertThat(savedCustomer.getNickname()).isEqualTo(req.getNickname());

        verify(redisTemplate).delete("signup:" + req.getEmail());
    }

    @Test
    void signup_WhenEmailAlreadyExists_ThrowsException() {
        // given
        SignupRequestDto req = new SignupRequestDto("test@example.com", "Password1!", "홍길동", "길동이", "010-1234-1234");
        given(customerRepository.findByEmail(req.getEmail())).willReturn(Optional.of(mock(Customer.class)));

        // when & then
        assertThatThrownBy(() -> authService.tempSignup(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 존재하는 이메일");
    }

    @Test
    void login_Admin_ReturnsToken() {
        String email = "admin@test.com";
        String rawPassword = "adminPass";
        Admin admin = mock(Admin.class);
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        given(adminRepository.findByEmail(email)).willReturn(Optional.of(admin));
        given(admin.getPassword()).willReturn("encoded");
        given(admin.getId()).willReturn(id);
        given(passwordEncoder.matches(rawPassword, "encoded")).willReturn(true);
        given(jwtTokenProvider.createToken(id, "admin")).willReturn("access-token");


        LoginResponseDto res = authService.login(email, rawPassword);


        assertThat(res.getToken()).isEqualTo("access-token");
        assertThat(res.getRefreshToken()).isNull();
        assertThat(res.getType()).isEqualTo("admin");
    }

    @Test
    void login_Manager_ReturnsToken() {
        String email = "manager@test.com";
        String rawPassword = "managerPass";
        Manager manager = mock(Manager.class);
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        given(adminRepository.findByEmail(email)).willReturn(Optional.empty());
        given(managerRepository.findByEmail(email)).willReturn(Optional.of(manager));
        given(manager.getPassword()).willReturn("encoded");
        given(manager.getId()).willReturn(id);
        given(passwordEncoder.matches(rawPassword, "encoded")).willReturn(true);
        given(jwtTokenProvider.createToken(id, "manager")).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(id, "manager")).willReturn("refresh-token");

        // 💡 null 허용 matcher 사용
        doNothing().when(refreshTokenService).saveOrUpdateToken(
                any(),
                nullable(String.class),
                any(LocalDateTime.class)
        );

        LoginResponseDto res = authService.login(email, rawPassword);

        assertThat(res.getToken()).isEqualTo("access-token");
        assertThat(res.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(res.getType()).isEqualTo("manager");
    }


    // 로그인 테스트: Customer
    @Test
    void login_Customer_ReturnsToken() {
        String email = "user@test.com";
        String rawPassword = "userPass";
        Customer customer = mock(Customer.class);
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        given(adminRepository.findByEmail(email)).willReturn(Optional.empty());
        given(managerRepository.findByEmail(email)).willReturn(Optional.empty());
        given(customerRepository.findByEmail(email)).willReturn(Optional.of(customer));
        given(customer.getPassword()).willReturn("encoded");
        given(customer.getId()).willReturn(id);
        given(passwordEncoder.matches(rawPassword, "encoded")).willReturn(true);
        given(jwtTokenProvider.createToken(id, "customer")).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(id, "customer")).willReturn("refresh-token");

        doNothing().when(refreshTokenService).saveOrUpdateToken(
                any(),
                nullable(String.class),
                any(LocalDateTime.class)
        );

        LoginResponseDto res = authService.login(email, rawPassword);

        assertThat(res.getToken()).isEqualTo("access-token");
        assertThat(res.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(res.getType()).isEqualTo("customer");
    }

    @Test
    void login_WhenPasswordInvalid_ThrowsException() {
        String email = "admin@test.com";
        Admin admin = mock(Admin.class);

        given(adminRepository.findByEmail(email)).willReturn(Optional.of(admin));
        given(admin.getPassword()).willReturn("encoded");
        given(passwordEncoder.matches(any(), any())).willReturn(false);

        assertThatThrownBy(() -> authService.login(email, "wrongPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호");
    }

    @Test
    void login_WhenEmailNotFound_ThrowsException() {
        String email = "nouser@test.com";

        given(adminRepository.findByEmail(email)).willReturn(Optional.empty());
        given(managerRepository.findByEmail(email)).willReturn(Optional.empty());
        given(customerRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(email, "pass"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void changePassword_Admin_Success() {
        UUID userId = UUID.randomUUID();
        String currentPassword = "oldPass";
        String newPassword = "newPass";

        Admin admin = new Admin();
        admin.setId(userId);
        admin.setPassword("encodedOldPass");

        given(adminRepository.findById(userId)).willReturn(Optional.of(admin));
        given(passwordEncoder.matches(currentPassword, admin.getPassword())).willReturn(true);
        given(passwordEncoder.encode(newPassword)).willReturn("encodedNewPass");
        given(adminRepository.save(any(Admin.class))).willReturn(admin);

        authService.changePassword(userId.toString(), currentPassword, newPassword);

        verify(adminRepository).save(argThat(savedAdmin ->
                savedAdmin.getPassword().equals("encodedNewPass")
        ));
    }

    @Test
    void changePassword_Customer_Success() {
        UUID userId = UUID.randomUUID();
        String currentPassword = "oldPass";
        String newPassword = "newPass";

        Customer customer = new Customer();
        customer.setId(userId);
        customer.setPassword("encodedOldPass");

        given(adminRepository.findById(userId)).willReturn(Optional.empty());
        given(managerRepository.findById(userId)).willReturn(Optional.empty());
        given(customerRepository.findById(userId)).willReturn(Optional.of(customer));
        given(passwordEncoder.matches(currentPassword, customer.getPassword())).willReturn(true);
        given(passwordEncoder.encode(newPassword)).willReturn("encodedNewPass");

        authService.changePassword(userId.toString(), currentPassword, newPassword);

        verify(customerRepository).save(argThat(savedCustomer ->
                savedCustomer.getPassword().equals("encodedNewPass")
        ));
    }

    @Test
    void changePassword_UserNotFound_Throws() {
        UUID userId = UUID.randomUUID();
        String currentPassword = "oldPass";
        String newPassword = "newPass";

        given(adminRepository.findById(userId)).willReturn(Optional.empty());
        given(managerRepository.findById(userId)).willReturn(Optional.empty());
        given(customerRepository.findById(userId)).willReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                authService.changePassword(userId.toString(), currentPassword, newPassword)
        );
    }

    @Test
    void changePassword_WrongCurrentPassword_Throws() {
        UUID userId = UUID.randomUUID();
        String currentPassword = "wrongOldPass";
        String newPassword = "newPass";

        Customer customer = new Customer();
        customer.setId(userId);
        customer.setPassword("encodedOldPass");

        given(adminRepository.findById(userId)).willReturn(Optional.empty());
        given(managerRepository.findById(userId)).willReturn(Optional.empty());
        given(customerRepository.findById(userId)).willReturn(Optional.of(customer));
        given(passwordEncoder.matches(currentPassword, customer.getPassword())).willReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                authService.changePassword(userId.toString(), currentPassword, newPassword)
        );

        verify(customerRepository, never()).save(any());
    }
}

