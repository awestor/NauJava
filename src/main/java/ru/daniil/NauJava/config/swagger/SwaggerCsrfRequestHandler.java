package ru.daniil.NauJava.config.swagger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class SwaggerCsrfRequestHandler extends CsrfTokenRequestAttributeHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       Supplier<CsrfToken> csrfToken) {
        Boolean isFromSwagger = (Boolean) request.getAttribute("IS_FROM_SWAGGER");

        if (isFromSwagger != null && isFromSwagger) {
            return;
        }

        super.handle(request, response, csrfToken);
    }
}
