package com.example.springbootagrolink.config;

import com.example.springbootagrolink.model.Rol;
import com.example.springbootagrolink.model.Usuario;
import com.example.springbootagrolink.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsuarios(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Crear admin si no existe
            usuarioRepository.findByNombreUsuario("admin").ifPresentOrElse(u -> {
                // ya existe
            }, () -> {
                Usuario admin = new Usuario();
                admin.setNombre("Admin");
                admin.setApellido("Admin");
                admin.setNombreUsuario("admin");
                admin.setContrasenaUsuario(passwordEncoder.encode("admin123"));
                admin.setCorreo("admin@local");
                admin.setCiudad("Local");
                admin.setDepartamento("Local");
                admin.setDireccion("Administración");
                admin.setCedula("0000000000");
                admin.setTelefono("0000000000");
                admin.setRol(Rol.ROLE_ADMIN);
                usuarioRepository.save(admin);
            });

            // Migrar contraseñas existentes: si no parecen BCrypt, encriptarlas
            List<Usuario> usuarios = usuarioRepository.findAll();
            for (Usuario u : usuarios) {
                String pass = u.getContrasenaUsuario();
                if (pass == null) continue;
                boolean isBCrypt = pass.startsWith("$2a$") || pass.startsWith("$2b$") || pass.startsWith("$2y$");
                if (!isBCrypt) {
                    u.setContrasenaUsuario(passwordEncoder.encode(pass));
                    usuarioRepository.save(u);
                }
            }
        };
    }
}

