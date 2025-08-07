package profect.eatcloud.login.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import profect.eatcloud.common.ApiResponse;
import profect.eatcloud.login.dto.LoginRequestDto;
import profect.eatcloud.login.dto.LoginResponseDto;
import profect.eatcloud.login.dto.SignupRequestDto;
import profect.eatcloud.login.service.AuthService;
import profect.eatcloud.login.service.RefreshTokenService;
import profect.eatcloud.security.jwt.JwtTokenProvider;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks private AuthController authController;

    @Mock private AuthService authService;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;

    @Test
    void login_ReturnsAccessAndRefreshToken() {
        // given
        LoginRequestDto request = new LoginRequestDto("test@example.com", "password");
        LoginResponseDto response = new LoginResponseDto("accessToken", "refreshToken", "Customer");

        given(authService.login(request.getEmail(), request.getPassword())).willReturn(response);

        // when
        ApiResponse<LoginResponseDto> result = authController.login(request);

        // then
        assertThat(result.getData()).isEqualTo(response);
        assertThat(result.getCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void register_CallsAuthServiceTempSignup() {
        // given
        SignupRequestDto request = new SignupRequestDto("email@example.com", "Password1!", "이름", "닉네임", "010-1234-5678");

        // when
        ApiResponse<Void> result = authController.register(request);

        // then
        verify(authService).tempSignup(request);
        assertThat(result.getCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void confirmEmail_CallsAuthServiceConfirmEmail() {
        // given
        String email = "test@example.com";
        String code = "123456";

        // when
        ApiResponse<Void> result = authController.confirmEmail(email, code);

        // then
        verify(authService).confirmEmail(email, code);
        assertThat(result.getCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void refreshToken_ValidToken_ReturnsNewTokens() {
        // given
        String refreshToken = "valid.refresh.token";
        UUID userId = UUID.randomUUID();
        String role = "Customer";
        Object user = new Object();

        given(jwtTokenProvider.getIdFromToken(refreshToken)).willReturn(userId);
        given(jwtTokenProvider.getTypeFromToken(refreshToken)).willReturn(role);
        given(refreshTokenService.findUserByRoleAndId(role, userId)).willReturn(user);
        given(refreshTokenService.isValid(user, refreshToken)).willReturn(true);
        given(jwtTokenProvider.createToken(userId, role)).willReturn("newAccessToken");
        given(jwtTokenProvider.createRefreshToken(userId, role)).willReturn("newRefreshToken");

        // when
        ApiResponse<LoginResponseDto> response = authController.refreshToken(refreshToken);

        // then
        verify(refreshTokenService).saveOrUpdateToken(eq(user), anyString(), any());
        assertThat(response.getData().getToken()).isEqualTo("newAccessToken");
        assertThat(response.getData().getRefreshToken()).isEqualTo("newRefreshToken");
    }

    @Test
    void logout_BlacklistsAccessTokenAndDeletesRefreshToken() {
        // given
        UUID userId = UUID.randomUUID();
        String role = "Customer";
        String token = "access.jwt.token";
        Object user = new Object();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(jwtTokenProvider.getIdFromToken(token)).willReturn(userId);
        given(jwtTokenProvider.getTypeFromToken(token)).willReturn(role);
        given(refreshTokenService.findUserByRoleAndId(role, userId)).willReturn(user);
        given(jwtTokenProvider.getExpirationTime(token)).willReturn(System.currentTimeMillis() + 60000); // 1분 후 만료

        // when
        ApiResponse<Void> response = authController.logout("Bearer " + token);

        // then
        verify(refreshTokenService).delete(user);
        verify(redisTemplate.opsForValue()).set(startsWith("blacklist:access:"), eq("blacklisted"), anyLong(), eq(TimeUnit.SECONDS));
        assertThat(response.getCode()).isEqualTo(HttpStatus.OK.value());
    }
}