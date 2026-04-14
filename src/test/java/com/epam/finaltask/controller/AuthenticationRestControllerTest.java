package com.epam.finaltask.controller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.auth.LoginRequest;
import com.epam.finaltask.dto.auth.SignUpRequest;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.repository.RefreshTokenRepository;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.security.JwtService;
import com.epam.finaltask.service.security.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test_user", roles = {"USER"})
    void logout_WithValidCookie_ShouldClearCookiesAndReturnOk() throws Exception {
        Cookie refreshCookie = new Cookie("refresh_jwt", "test-dummy-token");

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/auth/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refresh_jwt", 0))
                .andExpect(cookie().path("refresh_jwt", "/"))
                .andExpect(cookie().maxAge("jwt", 0))
                .andExpect(cookie().path("jwt", "/"))
                .andExpect(header().string("Authorization", ""));

        verify(refreshTokenRepository, times(1)).findByToken("test-dummy-token");
    }

    @Test
    void signin_WithValidCredentials_ShouldReturnOkAndSetCookies() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("fake-access-token");
        when(jwtService.getMaxAgeSeconds()).thenReturn(3600);

        RefreshToken fakeRefreshToken = new RefreshToken();
        fakeRefreshToken.setToken("fake-refresh-token");

        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(fakeRefreshToken);
        when(refreshTokenService.getMaxAgeSeconds()).thenReturn(86400);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(fakeRefreshToken);

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("jwt", "fake-access-token"))
                .andExpect(cookie().value("refresh_jwt", "fake-refresh-token"));
    }

    @Test
    void signin_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("wronguser", "wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signup_NewUser_ShouldReturnCreated() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setPassword("password123");
        signUpRequest.setPhoneNumber("+3801111111");

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");

        when(userMapper.toUserDTO(any(SignUpRequest.class))).thenReturn(userDTO);

        when(userService.register(any(UserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated());

        verify(userService, times(1)).register(any(UserDTO.class));
    }
}
