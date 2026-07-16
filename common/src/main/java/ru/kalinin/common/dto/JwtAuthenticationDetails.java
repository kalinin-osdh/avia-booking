package ru.kalinin.common.dto;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Getter
public class JwtAuthenticationDetails extends WebAuthenticationDetails {
    private final Claims claims;

    public JwtAuthenticationDetails(HttpServletRequest request, Claims claims) {
        super(request);
        this.claims = claims;
    }

}
