package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.auth.AuthRequest;
import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.repository.RefreshTokenRepository;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.security.JwtService;
import com.epam.finaltask.service.security.RefreshTokenService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        UserDetails userDetails = userRepository.findUserByUsername(authRequest.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Bad username or password"));

        if (!passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Bad username or password");
        }

        RefreshToken refreshToken = refreshTokenRepository
                .save(refreshTokenService.createRefreshToken(userDetails.getUsername()));
        response.setHeader("Authorization", "Bearer " + jwtService.generateToken(userDetails));
        Cookie refresh = new Cookie("refresh_jwt", refreshToken.getToken());
        refresh.setHttpOnly(true);
        response.addCookie(refresh);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletResponse response, @CookieValue("refresh_jwt") String refreshToken) {
        RefreshToken refresh = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new EntityNotFoundException("Token not found!"));
        refresh = refreshTokenService.verifyExpiration(refresh);
        response.setHeader("Authorization", "Bearer " + jwtService.generateToken(refresh.getUser()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, @CookieValue("refresh_jwt") String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new EntityNotFoundException("Token not found!")));
        response.setHeader("Authorization", "");
        response.addCookie(new Cookie("refresh_jwt", null));
        return ResponseEntity.ok().build();
    }
}
