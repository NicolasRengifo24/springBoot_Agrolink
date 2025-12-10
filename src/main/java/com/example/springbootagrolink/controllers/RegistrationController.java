package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Rol;
import com.example.springbootagrolink.model.Usuario;
import com.example.springbootagrolink.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    public RegistrationController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String nombre,
                               @RequestParam String apellido,
                               @RequestParam String nombreUsuario,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam(required = false) String role,
                               @RequestParam(required = false) String ciudad,
                               @RequestParam(required = false) String departamento,
                               @RequestParam(required = false) String direccion,
                               @RequestParam(required = false) String cedula,
                               @RequestParam(required = false) String telefono,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Validaciones mínimas
        if (nombreUsuario == null || nombreUsuario.isBlank() || password == null || password.isBlank() || email == null || email.isBlank()) {
            model.addAttribute("error", "Completa los campos obligatorios (usuario, email, contraseña).");
            return "register";
        }

        if (usuarioRepository.findByNombreUsuario(nombreUsuario).isPresent()) {
            model.addAttribute("error", "El nombre de usuario ya está en uso.");
            return "register";
        }

        // Crear usuario
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setNombreUsuario(nombreUsuario);
        u.setContrasenaUsuario(passwordEncoder.encode(password));
        u.setCorreo(email);
        u.setCiudad(ciudad == null ? "" : ciudad);
        u.setDepartamento(departamento == null ? "" : departamento);
        u.setDireccion(direccion == null ? "" : direccion);
        u.setCedula(cedula == null ? "" : cedula);
        u.setTelefono(telefono == null ? "0000000000" : telefono);
        // Validar role solicitado. No permitir ROLE_ADMIN desde el formulario.
        Rol assignedRole = Rol.ROLE_CLIENTE; // default
        if (role != null && !role.isBlank()) {
            try {
                // role parameter expected values: CLIENTE, PRODUCTOR, TRANSPORTISTA, SERVICIO
                String normalized = role.trim().toUpperCase();
                switch (normalized) {
                    case "CLIENTE":
                        assignedRole = Rol.ROLE_CLIENTE; break;
                    case "PRODUCTOR":
                        assignedRole = Rol.ROLE_PRODUCTOR; break;
                    case "TRANSPORTISTA":
                        assignedRole = Rol.ROLE_TRANSPORTISTA; break;
                    case "SERVICIO":
                        assignedRole = Rol.ROLE_SERVICIO; break;
                    default:
                        assignedRole = Rol.ROLE_CLIENTE; break;
                }
            } catch (Exception ex) {
                assignedRole = Rol.ROLE_CLIENTE;
            }
        }
        u.setRol(assignedRole);

        try {
            usuarioRepository.save(u);
            logger.info("Usuario registrado: {} con rol={}", nombreUsuario, u.getRol());
            redirectAttributes.addAttribute("registered", "true");
            return "redirect:/login";
        } catch (DataIntegrityViolationException ex) {
            logger.warn("Error al registrar usuario (integridad): {}", ex.getMessage());
            model.addAttribute("error", "No se pudo crear el usuario. Verifica los datos e inténtalo de nuevo.");
            return "register";
        } catch (Exception ex) {
            logger.error("Error inesperado al registrar usuario: {}", ex.getMessage());
            model.addAttribute("error", "Ocurrió un error. Intenta nuevamente más tarde.");
            return "register";
        }
    }
}
