package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.auth.AuthRequest;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        UserDetails userDetails = userRepository.findUserByUsername(authRequest.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Bad username or password"));

        if (!passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Bad username or password");
        }
        Cookie jwt = new Cookie("jwt", jwtService.generateToken(userDetails));
        jwt.setHttpOnly(true);
        response.addCookie(jwt);
        return ResponseEntity.ok().build();
    }
}
