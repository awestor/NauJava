package ru.daniil.NauJava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import ru.daniil.NauJava.config.jwt.JwtAuthenticationFilter;
import ru.daniil.NauJava.config.swagger.CsrfRequestMatcher;
import ru.daniil.NauJava.config.swagger.SwaggerCsrfRequestHandler;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfig baseCorsConfig;

    public SpringSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                                CorsConfig baseCorsConfig,
                                SwaggerCsrfRequestHandler swaggerCsrfRequestHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.baseCorsConfig = baseCorsConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        return security
                .cors(cors -> cors.configurationSource(baseCorsConfig.corsConfigurationSource()))

                .addFilterBefore(jwtAuthenticationFilter, CsrfFilter.class)

                .csrf(csrf -> csrf
                        .requireCsrfProtectionMatcher(new CsrfRequestMatcher())
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
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
                                "/api/daily-reports/**",
                                "/api/auth/token",
                                "/api/auth/validate"
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}