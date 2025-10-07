package com.ayungi.cms.service;

import com.ayungi.cms.dto.request.LoginRequest;
import com.ayungi.cms.dto.request.RegisterRequest;
import com.ayungi.cms.dto.response.AuthResponse;
import com.ayungi.cms.entity.Role;
import com.ayungi.cms.entity.User;
import com.ayungi.cms.repository.RoleRepository;
import com.ayungi.cms.repository.UserRepository;
import com.ayungi.cms.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Тесты для AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_WithValidData_ShouldReturnAuthResponse() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        Role userRole = new Role();
        userRole.setName("USER");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .password("encodedPassword")
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));
        when(jwtUtil.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refreshToken");

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals(request.getUsername(), response.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        Role userRole = new Role();
        userRole.setName("USER");

        User user = User.builder()
                .username(request.getUsername())
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refreshToken");

        // When
        AuthResponse response = authService.login(request, "127.0.0.1");

        // Then
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(authenticationManager).authenticate(any());
    }
}
