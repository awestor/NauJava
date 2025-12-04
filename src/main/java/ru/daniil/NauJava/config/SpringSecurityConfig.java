package ru.daniil.NauJava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import ru.daniil.NauJava.config.filter.SwaggerCsrfRequestHandler;
import ru.daniil.NauJava.config.filter.SwaggerRequestFilter;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    private final SwaggerRequestFilter swaggerRequestFilter;
    private final SwaggerCsrfRequestHandler swaggerCsrfRequestHandler;

    public SpringSecurityConfig(SwaggerRequestFilter swaggerRequestFilter,
                          SwaggerCsrfRequestHandler swaggerCsrfRequestHandler) {
        this.swaggerRequestFilter = swaggerRequestFilter;
        this.swaggerCsrfRequestHandler = swaggerCsrfRequestHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        return security
                .addFilterBefore(swaggerRequestFilter, CsrfFilter.class)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(swaggerCsrfRequestHandler)
                        .ignoringRequestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/favicon.ico"
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/mainPage/**",
                                "/register",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api/reports/**",
                                "/view/reports/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/view/products/**",
                                "/api/products/**",
                                "/view/meals/**",
                                "/api/meals/**",
                                "/view/daily-reports/**"
                        ).hasRole("USER")
                        .requestMatchers(
                                "/api/account/**",
                                "/view/account",
                                "/api/profile/**",
                                "/api/daily-reports/**"
                        ).hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .build();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        return repository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}