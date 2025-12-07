package ru.daniil.NauJava.config.swagger;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SwaggerRequestFilter extends OncePerRequestFilter {

    private static final List<String> SWAGGER_PATHS = List.of(
            "/v3/api-docs",
            "/v3/api-docs/swagger-config",
            "/swagger-ui",
            "/swagger-ui/",
            "/swagger-ui/index.html",
            "/swagger-ui/swagger-initializer.js",
            "/swagger-ui/swagger-ui-bundle.js",
            "/swagger-ui/swagger-ui-standalone-preset.js",
            "/swagger-ui/swagger-ui.css",
            "/swagger-ui/favicon-32x32.png",
            "/swagger-ui/favicon-16x16.png"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String referer = request.getHeader("Referer");

        boolean isFromSwagger = isSwaggerRequest(path, referer);

        request.setAttribute("IS_FROM_SWAGGER", isFromSwagger);

        filterChain.doFilter(request, response);
    }

    private boolean isSwaggerRequest(String path, String referer) {
        if (path != null) {
            if (SWAGGER_PATHS.stream().anyMatch(path::startsWith)) {
                return true;
            }

            return path.startsWith("/api/") && referer != null &&
                    (referer.contains("/swagger-ui/") || referer.contains("swagger-ui/index.html"));
        }

        return false;
    }
}
