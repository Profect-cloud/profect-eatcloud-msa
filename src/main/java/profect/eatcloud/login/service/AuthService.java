package profect.eatcloud.login.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AuthService {

	private final CustomerRepository customerRepository;
	private final AdminRepository adminRepository;
	private final ManagerRepository managerRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, Object> redisTemplate;
	private final MailService mailService;
	private final RefreshTokenService refreshTokenService;

	// 1) 로그인
	public LoginResponseDto login(String email, String password) {
		// 1) Admin 먼저 조회
		Admin admin = adminRepository.findByEmail(email)
			.orElse(null);
		if (admin != null) {
			if (!passwordEncoder.matches(password, admin.getPassword())) {
				throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
			}
			// Admin 전용 토큰(리프레시 토큰은 사용하지 않음)
			String accessToken = jwtTokenProvider.createToken(admin.getId(), "admin");
			return new LoginResponseDto(accessToken, null, "admin");
		}

		// 2) Manager 조회
		Manager manager = managerRepository.findByEmail(email)
			.orElse(null);
		if (manager != null) {
			if (!passwordEncoder.matches(password, manager.getPassword())) {
				throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
			}
			// 방문 로그 기록 등 필요시 추가
			// User/Manager용 Access + Refresh 토큰 생성
			String accessToken = jwtTokenProvider.createToken(manager.getId(), "manager");
			String refreshToken = jwtTokenProvider.createRefreshToken(manager.getId(), "manager");
			// 리프레시 토큰 DB에 저장 또는 갱신 (추후 활성화)
			LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
			refreshTokenService.saveOrUpdateToken(manager, refreshToken, expiryDate);
			return new LoginResponseDto(accessToken, refreshToken, "manager");
		}

		// 3) User 조회
		Customer customer = customerRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다: " + email));

		if (!passwordEncoder.matches(password, customer.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		// 방문 로그 기록
		//visitLogService.logVisit(user.getId());

		// Access + Refresh 토큰 생성
		String accessToken = jwtTokenProvider.createToken(customer.getId(), "customer");
		String refreshToken = jwtTokenProvider.createRefreshToken(customer.getId(), "customer");

		// 리프레시 토큰 DB에 저장 또는 갱신
		LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
		refreshTokenService.saveOrUpdateToken(customer, refreshToken, expiryDate);

		return new LoginResponseDto(accessToken, refreshToken, "customer");
	}

	// 2) 회원가입 (Customer 예시)
	public void tempSignup(SignupRequestDto req) {
		if (customerRepository.findByEmail(req.getEmail()).isPresent()) {
			throw new RuntimeException("이미 존재하는 이메일입니다.");
		}

		String verificationCode = UUID.randomUUID().toString().substring(0, 6); // 간단한 코드

		// 이메일 본문 작성
		String subject = "이메일 인증 코드";
		String text = "회원가입 인증 코드: " + verificationCode;

		// 이메일 전송
		try {
			mailService.sendMail(req.getEmail(), subject, text);
		} catch (MailException e) {
			throw new RuntimeException("이메일 전송에 실패했습니다. 다시 시도해주세요.", e);
		}

		// Redis에 저장 (key: "signup:{email}", value: SignupRequestDto+코드)
		SignupRedisData data = new SignupRedisData(req, verificationCode);
		redisTemplate.opsForValue().set("signup:" + req.getEmail(), data, 10, TimeUnit.MINUTES); // 10분 유효
	}

	public void confirmEmail(String email, String code) {
		String key = "signup:" + email;
		SignupRedisData data = (SignupRedisData)redisTemplate.opsForValue().get(key);

		if (data == null) {
			throw new RuntimeException("만료되었거나 존재하지 않는 인증 요청입니다.");
		}

		if (!data.getCode().equals(code)) {
			throw new RuntimeException("인증 코드가 일치하지 않습니다.");
		}

		// 최종 가입 처리
		Customer customer = Customer.builder()
			.email(data.getRequest().getEmail())
			.password(passwordEncoder.encode(data.getRequest().getPassword()))
			.name(data.getRequest().getName())
			.nickname(data.getRequest().getNickname())
			.build();
		customerRepository.save(customer);

		// Redis에서 제거
		redisTemplate.delete(key);
	}

	public void signupWithoutEmailVerification(SignupRequestDto req) {
		if (customerRepository.findByEmail(req.getEmail()).isPresent()) {
			throw new RuntimeException("이미 존재하는 이메일입니다.");
		}
		Customer customer = Customer.builder()
				.email(req.getEmail())
				.password(passwordEncoder.encode(req.getPassword()))
				.name(req.getName())
				.nickname(req.getNickname())
				.build();
		customerRepository.save(customer);
	}

	public void changePassword(String userId, String currentPassword, String newPassword) {
		Admin admin = adminRepository.findById(UUID.fromString(userId)).orElse(null);
		if (admin != null) {
			if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
				throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
			}
			admin.setPassword(passwordEncoder.encode(newPassword));
			adminRepository.save(admin);
			return;
		}

		Manager manager = managerRepository.findById(UUID.fromString(userId)).orElse(null);
		if (manager != null) {
			if (!passwordEncoder.matches(currentPassword, manager.getPassword())) {
				throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
			}
			manager.setPassword(passwordEncoder.encode(newPassword));
			managerRepository.save(manager);
			return;
		}

		Customer customer = customerRepository.findById(UUID.fromString(userId))
			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

		if (!passwordEncoder.matches(currentPassword, customer.getPassword())) {
			throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
		}

		customer.setPassword(passwordEncoder.encode(newPassword));
		customerRepository.save(customer);
	}
}
