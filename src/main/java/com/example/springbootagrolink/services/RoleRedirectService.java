package com.example.springbootagrolink.services;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

// Clase mantenida solo como referencia. La anotación @Service se quitó para evitar que
// se registre como bean en el contexto de Spring, tal como solicitó el usuario.
public class RoleRedirectService {

    public String determineRedirectUrl(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) {
            return "/";
        }

        // Prioridad: ADMIN > TRANSPORTISTA > PRODUCTOR > SERVICIO > CLIENTE
        if (authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            return "/admin";
        }
        if (authorities.stream().anyMatch(a -> "ROLE_TRANSPORTISTA".equals(a.getAuthority()))) {
            return "/transportista/envios";
        }
        if (authorities.stream().anyMatch(a -> "ROLE_PRODUCTOR".equals(a.getAuthority()))) {
            return "/productos/dashboard";
        }
        if (authorities.stream().anyMatch(a -> "ROLE_SERVICIO".equals(a.getAuthority()))) {
            return "/servicios";
        }
        if (authorities.stream().anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()))) {
            return "/cliente/index";
        }

        return "/"; // fallback
    }
}
