package ru.kalinin.auth.dto.mapper;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.kalinin.auth.dto.request.AuthRequest;
import ru.kalinin.auth.dto.response.AuthResponse;
import ru.kalinin.auth.entity.User;

import java.util.Collections;

@Component
public class UserMapper {

    public User toEntity(AuthRequest request){
        return User.builder()
                .username(request.getUsername())
                .build();
    }

    public AuthResponse toAuthResponse(String username, String accessToken, String refreshToken){
        return new AuthResponse(username,accessToken,refreshToken);
    }

    public UserDetails toUserDetails(User user){
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().toString()))
        );
    }
}
