package ru.daniil.NauJava.config.swagger;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

public class CsrfRequestMatcher implements RequestMatcher {

    private static final Logger logger = LoggerFactory.getLogger(CsrfRequestMatcher.class);

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/token",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/favicon.ico"
    );

    private static final List<String> SAFE_METHODS = List.of(
            "GET", "HEAD", "TRACE", "OPTIONS"
    );

    @Override
    public boolean matches(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("CsrfRequestMatcher проверяет {} {}", method, path);

        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.startsWith(excludedPath)) {
                logger.debug("Путь {} исключен из CSRF проверки", path);
                return false;
            }
        }

        if (SAFE_METHODS.contains(method)) {
            return false;
        }

        logger.debug("CSRF требуется для {} {}", method, path);
        return true;
    }
}