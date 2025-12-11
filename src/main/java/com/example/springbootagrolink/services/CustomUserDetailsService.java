package com.example.springbootagrolink.services;

import com.example.springbootagrolink.model.Usuario;
import com.example.springbootagrolink.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado en la BD: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        logger.info("Cargando usuario '{}' desde BD (id={}) con rol={}", usuario.getNombreUsuario(), usuario.getIdUsuario(), usuario.getRol());

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(usuario.getRol().name()));

        logger.info("Authorities del usuario {}: {}", usuario.getNombreUsuario(), authorities);

        return new org.springframework.security.core.userdetails.User(
                usuario.getNombreUsuario(),
                usuario.getContrasenaUsuario(),
                authorities
        );
    }
}
