package ru.daniil.NauJava.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final String CSRF_SKIP_ATTRIBUTE = "SHOULD_NOT_FILTER" + CsrfFilter.class.getName();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = resolveToken(request);
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("JWT Filter: обработка {} {}", method, requestURI);

        if (StringUtils.hasText(jwt)) {
            logger.debug("Найден JWT токен для запроса {}", requestURI);

            if (jwtTokenProvider.validateToken(jwt)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                CsrfFilter.skipRequest(request);

                logger.debug("Аутентифицирован через JWT: {}. CSRF проверка отключена.",
                        authentication.getName());
            } else {
                logger.warn("Невалидный JWT токен для запроса {}", requestURI);
            }
        } else {
            logger.debug("JWT токен не найден, CSRF проверка будет выполнена");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Этот фильтр должен работать для ВСЕХ запросов
     * @param request current HTTP request
     * @return всегда false
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return false;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}