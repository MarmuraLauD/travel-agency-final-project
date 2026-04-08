package com.epam.finaltask.repository;

import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    Optional<Void> deleteByUser(User user);
    Optional<RefreshToken> findByUser(UserDetails userDetails);
    Optional<Void> deleteByToken(RefreshToken refreshToken);
}
