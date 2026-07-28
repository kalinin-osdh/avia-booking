package ru.kalinin.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kalinin.auth.dto.request.AuthRequest;
import ru.kalinin.auth.dto.request.RefreshRequest;
import ru.kalinin.auth.dto.response.AuthResponse;
import ru.kalinin.auth.entity.User;
import ru.kalinin.auth.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("auth_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();

        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void shouldRegisterUser() throws Exception {
        AuthRequest request = new AuthRequest(
                "kalinin",
                "kalinin17"
        );

        String jsonResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse response = objectMapper.readValue(
                jsonResponse,
                AuthResponse.class
        );

        assertNotNull(response);
        assertEquals("kalinin", response.getUsername());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        Optional<User> optionalUser = userRepository.findByUsername("kalinin");

        assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();
        assertEquals("kalinin", user.getUsername());
        assertNotEquals("kalinin17", user.getPassword());

        String refreshToken = redisTemplate.opsForValue().get("user_token:" + user.getId());

        assertNotNull(refreshToken);

        String userId = redisTemplate.opsForValue().get("refresh_token:" + refreshToken);

        assertNotNull(userId);

        assertEquals(user.getId(), Long.valueOf(userId));
    }

    @Test
    @DisplayName("Получение нового рефреш токена")
    void shouldRefreshUserToken() throws Exception {
        AuthRequest authRequest = new AuthRequest(
                "kalinin",
                "kalinin17"
        );

        String jsonRegisterResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse response = objectMapper.readValue(
                jsonRegisterResponse,
                AuthResponse.class
        );

        assertNotNull(response);
        assertEquals("kalinin", response.getUsername());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        Optional<User> optionalUser = userRepository.findByUsername("kalinin");

        assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();

        String oldRefreshToken = redisTemplate.opsForValue().get("user_token:" + user.getId());

        RefreshRequest refreshRequest = new RefreshRequest(oldRefreshToken);

        String jsonRefreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse refreshResponse = objectMapper.readValue(
                jsonRefreshResponse,
                AuthResponse.class
        );

        assertFalse(redisTemplate.hasKey("refresh_token:" + oldRefreshToken));

        assertNotNull(refreshResponse);
        assertEquals(response.getUsername(), refreshResponse.getUsername());
        assertEquals(response.getAccessToken(), refreshResponse.getAccessToken());
        assertNotEquals(response.getRefreshToken(), refreshResponse.getRefreshToken());
    }

    @Test
    @DisplayName("Успешный выход из сервиса")
    void shouldLogoutUser() throws Exception {
        AuthRequest authRequest = new AuthRequest(
                "kalinin",
                "kalinin17"
        );

        String jsonRegisterResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse registerResponse = objectMapper.readValue(
                jsonRegisterResponse,
                AuthResponse.class
        );

        assertNotNull(registerResponse);
        assertEquals("kalinin", registerResponse.getUsername());
        assertNotNull(registerResponse.getAccessToken());
        assertNotNull(registerResponse.getRefreshToken());

        Optional<User> optionalUser = userRepository.findByUsername("kalinin");

        assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();

        String refreshToken = redisTemplate.opsForValue().get("user_token:" + user.getId());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + registerResponse.getAccessToken()))
                .andExpect(status().isOk());

        assertFalse(redisTemplate.hasKey("refresh_token:" + refreshToken));
        assertFalse(redisTemplate.hasKey("user_token:" + user.getId()));
    }
}
