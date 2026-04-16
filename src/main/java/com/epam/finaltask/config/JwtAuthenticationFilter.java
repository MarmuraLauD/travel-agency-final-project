package com.epam.finaltask.config;

import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.repository.RefreshTokenRepository;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.security.JwtService;
import com.epam.finaltask.service.security.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull  FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = null;
        String refreshJwt = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) jwt = cookie.getValue();
                if ("refresh_jwt".equals(cookie.getName())) refreshJwt = cookie.getValue();
            }
        }

        if (jwt == null && refreshJwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwt == null && refreshJwt != null) {
            RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshJwt).orElse(null);
            if (refreshToken != null && !refreshTokenService.isExpired(refreshToken)) {
                UserDetails userDetails = refreshToken.getUser();
                jwt = jwtService.generateToken(userDetails);
                Cookie newJwtCookie = new Cookie("jwt", jwt);
                newJwtCookie.setPath("/");
                newJwtCookie.setHttpOnly(true);
                newJwtCookie.setMaxAge(jwtService.getMaxAgeSeconds());
                response.addCookie(newJwtCookie);
            }
        }

        if (jwt != null) {
            try {
                String username = jwtService.extractUsername(jwt);
                UserDetails userDetails = userMapper.toUser(userService.getUserByUsername(username));
                if (userDetails != null && jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ignored) {

            }
        }
        filterChain.doFilter(request, response);
    }
}
