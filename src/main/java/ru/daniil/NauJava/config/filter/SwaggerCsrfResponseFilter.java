package ru.daniil.NauJava.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class SwaggerCsrfResponseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String referer = request.getHeader("Referer");

        boolean isSwaggerApiRequest = path != null &&
                path.startsWith("/api/") &&
                referer != null &&
                referer.contains("/swagger-ui/");

        if (isSwaggerApiRequest) {
            String swaggerCsrfToken = "swagger-csrf-" + UUID.randomUUID().toString();

            response.setHeader("X-CSRF-TOKEN", swaggerCsrfToken);

            Cookie csrfCookie = new Cookie("XSRF-TOKEN", swaggerCsrfToken);
            csrfCookie.setPath("/");
            csrfCookie.setHttpOnly(false);
            response.addCookie(csrfCookie);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/");
    }
}
