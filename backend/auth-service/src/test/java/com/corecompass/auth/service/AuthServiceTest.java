package com.corecompass.auth.service;

import com.corecompass.auth.dto.AuthResponse;
import com.corecompass.auth.dto.LoginRequest;
import com.corecompass.auth.dto.RegisterRequest;
import com.corecompass.auth.entity.UserEntity;
import com.corecompass.auth.exception.DuplicateResourceException;
import com.corecompass.auth.exception.InvalidCredentialsException;
import com.corecompass.auth.repository.RefreshTokenRepository;
import com.corecompass.auth.repository.UserRepository;
import com.corecompass.auth.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock UserRepository        userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtService            jwtService;
    @Mock PasswordEncoder       passwordEncoder;
    @Mock HttpServletResponse   httpResponse;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirySeconds", 604800L);
    }

    // ─────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: happy path creates user and returns tokens")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("Password123!");
        req.setName("Test User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedpassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            return u;
        });
        when(jwtService.generateAccessToken(any(), anyString(), anyString()))
            .thenReturn("mocked.jwt.token");
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(900L);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedtoken");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse result = authService.register(req, httpResponse);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("register: throws DuplicateResourceException for existing email")
    void register_duplicateEmail_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@example.com");
        req.setPassword("Password123!");
        req.setName("Test User");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req, httpResponse))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("already exists");
    }

    // ─────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: happy path returns access token")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("Password123!");

        UserEntity user = UserEntity.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .passwordHash("$2a$12$hashedpassword")
            .name("Test User")
            .role("USER")
            .isActive(true)
            .build();

        when(userRepository.findByEmailAndIsDeletedFalse("user@example.com"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "$2a$12$hashedpassword")).thenReturn(true);
        when(jwtService.generateAccessToken(any(), anyString(), anyString()))
            .thenReturn("mocked.jwt.token");
        when(jwtService.getAccessTokenExpirySeconds()).thenReturn(900L);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedtoken");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse result = authService.login(req, httpResponse);

        assertThat(result.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(result.getUser().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("login: wrong password throws InvalidCredentialsException")
    void login_wrongPassword_throws() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrongpassword");

        UserEntity user = UserEntity.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .passwordHash("$2a$12$hashedpassword")
            .name("Test User")
            .role("USER")
            .isActive(true)
            .build();

        when(userRepository.findByEmailAndIsDeletedFalse("user@example.com"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "$2a$12$hashedpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req, httpResponse))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login: unknown email throws InvalidCredentialsException")
    void login_unknownEmail_throws() {
        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@example.com");
        req.setPassword("Password123!");

        when(userRepository.findByEmailAndIsDeletedFalse("nobody@example.com"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req, httpResponse))
            .isInstanceOf(InvalidCredentialsException.class);
    }
}
