package ru.kalinin.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kalinin.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}
