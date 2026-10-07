package com.example.Studio21.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    // En este avance las pantallas son públicas para revisar el maquetado.
    // La autenticación y los permisos reales se implementarán al conectar el backend.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(reglas -> reglas.anyRequest().permitAll());
        return http.build();
    }
}
