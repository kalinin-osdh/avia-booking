package ru.kalinin.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kalinin.auth.dto.mapper.UserMapper;
import ru.kalinin.auth.dto.request.AuthRequest;
import ru.kalinin.auth.dto.request.RefreshRequest;
import ru.kalinin.auth.dto.response.AuthResponse;
import ru.kalinin.auth.entity.User;
import ru.kalinin.auth.repository.UserRepository;
import ru.kalinin.auth.security.JwtTokenService;
import ru.kalinin.auth.service.interfaces.AuthService;
import ru.kalinin.auth.service.interfaces.RefreshTokenService;
import ru.kalinin.common.exception.refresh_token.RefreshTokenNotValid;
import ru.kalinin.common.exception.users.UserExistsException;
import ru.kalinin.common.exception.users.UserNotFoundException;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new UserExistsException(request.getUsername());

        String hashPassword = passwordEncoder.encode(request.getPassword());

        User user = userMapper.toEntity(request);
        user.setPassword(hashPassword);
        User savedUser = userRepository.save(user);

        String access = jwtTokenService.generateAccessToken(userMapper.toUserDetails(savedUser));
        String refresh = refreshTokenService.generateRefreshToken(savedUser.getId());

        return userMapper.toAuthResponse(savedUser.getUsername(), access, refresh);
    }

    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        String access = jwtTokenService.generateAccessToken(userMapper.toUserDetails(user));
        String refresh = refreshTokenService.generateRefreshToken(user.getId());

        return userMapper.toAuthResponse(user.getUsername(), access, refresh);
    }

    @Override
    public AuthResponse refresh(RefreshRequest request) {
        String token = request.getRefreshToken();

        Long userId = refreshTokenService.isRefreshTokenValid(token);

        if (userId == -1)
            throw new RefreshTokenNotValid();

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(userId)
        );

        String newAccessToken = jwtTokenService.generateAccessToken(userMapper.toUserDetails(user));
        String newRefreshToken = refreshTokenService.generateRefreshToken(user.getId());

        return userMapper.toAuthResponse(user.getUsername(), newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException(username)
        );

        refreshTokenService.deleteRefreshToken(user.getId());
    }

}
