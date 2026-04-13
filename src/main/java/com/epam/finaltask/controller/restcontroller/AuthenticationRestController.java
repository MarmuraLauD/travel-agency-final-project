package com.epam.finaltask.controller.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.auth.LoginRequest;
import com.epam.finaltask.dto.auth.SignUpRequest;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.repository.RefreshTokenRepository;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.security.JwtService;
import com.epam.finaltask.service.security.RefreshTokenService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/signup")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(userMapper.toUserDTO(signUpRequest)));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("User {} successfully logged in.", loginRequest.getUsername());
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        RefreshToken refreshToken = refreshTokenRepository
                .save(refreshTokenService.createRefreshToken(userDetails.getUsername()));
        String refresh = refreshToken.getToken();
        String jwt = jwtService.generateToken(userDetails);
        setCookie(response, "refresh_jwt", refresh, refreshTokenService.getMaxAgeSeconds());
        setCookie(response, "jwt", jwt, jwtService.getMaxAgeSeconds());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletResponse response, @CookieValue("refresh_jwt") String refreshToken) {
        RefreshToken refresh = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new EntityNotFoundException("Token not found!"));
        refresh = refreshTokenService.verifyExpiration(refresh);
        String jwt = jwtService.generateToken(refresh.getUser());
        setCookie(response, "jwt", jwt, jwtService.getMaxAgeSeconds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response,
                                    @CookieValue(value = "refresh_jwt", required = false) String refreshToken) {
        clearCookie(response, "refresh_jwt");
        clearCookie(response, "jwt");
        response.setHeader("Authorization", "");

        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
                log.info("Invalidating token for user: {}", token.getUser().getUsername());
                refreshTokenRepository.delete(token);
            });
        }
        return ResponseEntity.ok().build();
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void setCookie(HttpServletResponse response, String name, String value,  int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
