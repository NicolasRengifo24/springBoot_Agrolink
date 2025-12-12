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
        logger.info("=== Intentando cargar usuario: {} ===", username);

        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> {
                    logger.error("❌ Usuario NO encontrado en la BD: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        logger.info("✓ Usuario encontrado en BD:");
        logger.info("  - ID: {}", usuario.getIdUsuario());
        logger.info("  - Nombre: {}", usuario.getNombre());
        logger.info("  - Username: {}", usuario.getNombreUsuario());
        logger.info("  - Rol: {}", usuario.getRol());
        logger.info("  - Password hash: {}", usuario.getContrasenaUsuario().substring(0, Math.min(20, usuario.getContrasenaUsuario().length())) + "...");

        // Validar que el rol no sea nulo
        if (usuario.getRol() == null) {
            logger.error("❌ El usuario {} no tiene ROL asignado!", username);
            throw new UsernameNotFoundException("Usuario sin rol asignado: " + username);
        }

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(usuario.getRol().name()));

        logger.info("✓ Authorities asignadas: {}", authorities);

        return new org.springframework.security.core.userdetails.User(
                usuario.getNombreUsuario(),
                usuario.getContrasenaUsuario(),
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }
}
