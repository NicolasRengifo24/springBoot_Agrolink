package com.example.springbootagrolink.config;

import com.example.springbootagrolink.services.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler myAuthSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication) -> {
            try {
                Collection<? extends GrantedAuthority> auth = authentication.getAuthorities();
                String redirectUrl = "/";

                // Redirección basada en roles (prioridad: Admin > Transportista > Productor > Servicio > Cliente)
                if (auth.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
                    redirectUrl = "/admin";
                } else if (auth.stream().anyMatch(a -> "ROLE_TRANSPORTISTA".equals(a.getAuthority()))) {
                    redirectUrl = "/transportista/envios";
                } else if (auth.stream().anyMatch(a -> "ROLE_PRODUCTOR".equals(a.getAuthority()))) {
                    redirectUrl = "/productos/dashboard";
                } else if (auth.stream().anyMatch(a -> "ROLE_SERVICIO".equals(a.getAuthority()))) {
                    redirectUrl = "/servicio/dashboard";
                } else if (auth.stream().anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()))) {
                    redirectUrl = "/cliente/index";
                }

                // Log para debugging
                logger.info("Login exitoso: usuario='{}', authorities={} -> redirigiendo a {}",
                        authentication.getName(), auth, redirectUrl);

                response.sendRedirect(redirectUrl);
            } catch (Exception e) {
                logger.error("Error en redirección después del login", e);
                response.sendRedirect("/login?error");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Recursos públicos
                .requestMatchers("/css/**", "/CSS/**", "/static/**", "/js/**", "/images/**", "/imag/**", "/login", "/register", "/", "/buscar", "/access-denied").permitAll()
                // Rutas por rol
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/transportista/**").hasRole("TRANSPORTISTA")
                .requestMatchers("/servicio/**").hasRole("SERVICIO")
                .requestMatchers("/productos/**").hasRole("PRODUCTOR")
                .requestMatchers("/cliente/**", "/compras/**", "/pago/**").hasRole("CLIENTE")
                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .cacheControl(cache -> cache.disable())
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(myAuthSuccessHandler())
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(1209600) // 14 days
                .rememberMeParameter("remember-me")
                .userDetailsService(userDetailsService)
            )
            .exceptionHandling(e -> e.accessDeniedPage("/access-denied"))
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/logout")
            );

        return http.build();
    }
}
