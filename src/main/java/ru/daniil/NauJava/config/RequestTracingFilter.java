package ru.daniil.NauJava.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import ru.daniil.NauJava.utils.Loggers;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        String requestId = Loggers.initRequestContext(request);

        try {
            // Перехват статуса запроса
            ContentCachingResponseWrapper responseWrapper =
                    new ContentCachingResponseWrapper(response);

            chain.doFilter(request, responseWrapper);

            responseWrapper.copyBodyToResponse();

        } catch (Exception e) {
            Loggers.logError("Необработанное исключение в фильтре", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            Loggers.finishRequestContext(response.getStatus(), duration);
        }
    }
}
