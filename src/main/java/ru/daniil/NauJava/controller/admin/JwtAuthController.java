package ru.daniil.NauJava.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.daniil.NauJava.config.jwt.JwtTokenProvider;
import ru.daniil.NauJava.entity.Role;
import ru.daniil.NauJava.entity.User;
import ru.daniil.NauJava.service.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class JwtAuthController {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthController.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService authUserService;

    public JwtAuthController(JwtTokenProvider jwtTokenProvider,
                             UserService authUserService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authUserService = authUserService;
    }

    @GetMapping("/token")
    public ResponseEntity<?> getJwtToken() {
        logger.info("Запрос на получение JWT токена");

        Optional<User> userOptional = authUserService.getAuthUser();

        if (userOptional.isEmpty()) {
            logger.warn("Пользователь не аутентифицирован для получения JWT токена");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Не аутентифицирован");
            errorResponse.put("message", "Требуется аутентификация для получения JWT токена");
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        User user = userOptional.get();

        try {
            String jwt = jwtTokenProvider.generateTokenFromUser(user);

            logger.info("JWT токен сгенерирован для пользователя: {}", user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", jwt);
            response.put("tokenType", "Bearer");
            response.put("username", user.getUsername());
            response.put("userId", user.getId());
            response.put("roles", user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList()));
            response.put("expiresIn", jwtTokenProvider.getExpirationDateFromToken(jwt).getTime());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при генерации JWT токена: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка генерации токена");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Authentication createAuthentication(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> "ROLE_" + role.getName())
                        .toArray(String[]::new))
                .build();

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                user.getPassword(),
                userDetails.getAuthorities()
        );
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody String token) {

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.accepted()
                    .body(Map.of("valid", false, "message", "Невалидный токен"));
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("username", username);
        response.put("expiresAt", jwtTokenProvider.getExpirationDateFromToken(token));

        return ResponseEntity.ok(response);
    }
}