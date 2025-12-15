package ru.daniil.NauJava.config.swagger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.util.StringUtils;

public class HybridCsrfTokenRepository implements CsrfTokenRepository {

    private final HttpSessionCsrfTokenRepository sessionRepository = new HttpSessionCsrfTokenRepository();

    // Дополнительно для Swagger
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    public HybridCsrfTokenRepository() {
        sessionRepository.setSessionAttributeName("_csrf");
        sessionRepository.setParameterName("_csrf");
        sessionRepository.setHeaderName("X-CSRF-TOKEN");
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return sessionRepository.generateToken(request);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        sessionRepository.saveToken(token, request, response);

        if (token != null) {
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(CSRF_COOKIE_NAME, token.getToken());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(request.isSecure());
            response.addCookie(cookie);

            response.setHeader(CSRF_HEADER_NAME, token.getToken());
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        return sessionRepository.loadToken(request);
    }

    /**
     * Проверяет токен из нескольких источников
     */
    public boolean isValidToken(HttpServletRequest request, CsrfToken sessionToken, String requestToken) {
        if (sessionToken == null || !StringUtils.hasText(requestToken)) {
            return false;
        }

        // сессионный токен
        if (sessionToken.getToken().equals(requestToken)) {
            return true;
        }

        // cookie токен (для Swagger)
        String cookieToken = getTokenFromCookie(request);
        return StringUtils.hasText(cookieToken) && cookieToken.equals(requestToken);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (CSRF_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}