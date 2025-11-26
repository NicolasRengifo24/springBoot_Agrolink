package com.example.springbootagrolink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.example.springbootagrolink.model.Usuario;
import com.example.springbootagrolink.repository.UsuarioRepository;

@SpringBootApplication
public class SpringBootAgrolinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAgrolinkApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(UsuarioRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                Usuario u = new Usuario();
                u.setNombre("Juan");
                u.setApellido("Pérez");
                u.setNombreUsuario("juanp");
                u.setContrasenaUsuario("secret");
                u.setCorreo("juan.perez@example.com");
                u.setCiudad("Bogotá");
                u.setDepartamento("Cundinamarca");
                u.setDireccion("Calle Falsa 123");
                u.setCedula("1234567890");
                u.setTelefono("3000000000");
                repo.save(u);
                System.out.println("Usuario guardado: " + u.getCorreo());
            } else {
                System.out.println("Usuarios existentes: " + repo.count());
            }
        };
    }

}
