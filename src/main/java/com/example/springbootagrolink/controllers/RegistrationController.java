package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.*;
import com.example.springbootagrolink.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductorRepository productorRepository;

    @Autowired
    private TransportistaRepository transportistaRepository;

    @Autowired
    private AsesorRepository asesorRepository;

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
                               @RequestParam(required = false) String preferencias,
                               @RequestParam(required = false) String tipoCultivo,
                               @RequestParam(required = false) String zonasEntrega,
                               @RequestParam(required = false) String tipoAsesoria,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        logger.info("=== INICIO REGISTRO DE USUARIO ===");
        logger.info("Nombre de usuario: {}", nombreUsuario);

        if (nombreUsuario == null || nombreUsuario.isBlank() || password == null || password.isBlank() || email == null || email.isBlank()) {
            model.addAttribute("error", "Completa los campos obligatorios (usuario, email, contraseña).");
            return "register";
        }

        if (usuarioRepository.findByNombreUsuario(nombreUsuario).isPresent()) {
            model.addAttribute("error", "El nombre de usuario ya está en uso.");
            return "register";
        }

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

        Rol assignedRole = Rol.ROLE_CLIENTE;
        if (role != null && !role.isBlank()) {
            try {
                String normalized = role.trim().toUpperCase();
                if ("PRODUCTOR".equals(normalized)) {
                    assignedRole = Rol.ROLE_PRODUCTOR;
                } else if ("TRANSPORTISTA".equals(normalized)) {
                    assignedRole = Rol.ROLE_TRANSPORTISTA;
                } else if ("SERVICIO".equals(normalized)) {
                    assignedRole = Rol.ROLE_SERVICIO;
                }
            } catch (Exception ex) {
                assignedRole = Rol.ROLE_CLIENTE;
            }
        }
        u.setRol(assignedRole);

        try {
            logger.info("Guardando usuario en BD...");
            Usuario usuarioGuardado = usuarioRepository.saveAndFlush(u);
            logger.info("✓ Usuario guardado en BD con ID: {}", usuarioGuardado.getIdUsuario());

            try {
                logger.info("Creando registro específico para rol: {}", assignedRole);
                switch (assignedRole) {
                    case ROLE_CLIENTE:
                        crearClienteSinThrow(usuarioGuardado, preferencias);
                        break;
                    case ROLE_PRODUCTOR:
                        crearProductorSinThrow(usuarioGuardado, tipoCultivo);
                        break;
                    case ROLE_TRANSPORTISTA:
                        crearTransportistaSinThrow(usuarioGuardado, zonasEntrega);
                        break;
                    case ROLE_SERVICIO:
                        crearAsesorSinThrow(usuarioGuardado, tipoAsesoria);
                        break;
                }
                logger.info("✓✓✓ Registro completo exitoso");
            } catch (Exception roleEx) {
                logger.error("ERROR al crear registro específico de rol para {}: {}", nombreUsuario, roleEx.getMessage(), roleEx);
                logger.warn("El usuario fue creado pero faltó el registro específico de rol");
            }

            logger.info("=== FIN REGISTRO EXITOSO ===");
            redirectAttributes.addFlashAttribute("success", "Registro completado exitosamente. Por favor, inicia sesión.");
            return "redirect:/login";
        } catch (DataIntegrityViolationException ex) {
            logger.error("Error de integridad al registrar usuario: {}", ex.getMessage(), ex);
            model.addAttribute("error", "No se pudo crear el usuario. Verifica que el correo, usuario o cédula no estén ya registrados.");
            return "register";
        } catch (Exception ex) {
            logger.error("Error inesperado al registrar usuario: {}", ex.getMessage(), ex);
            model.addAttribute("error", "Ocurrió un error al procesar el registro. Por favor, inténtalo de nuevo.");
            return "register";
        }
    }

    protected void crearClienteSinThrow(Usuario usuario, String preferencias) {
        try {
            logger.info("=== INICIO crearCliente ===");
            logger.info("Usuario ID: {}", usuario.getIdUsuario());
            logger.info("Usuario nombre: {}", usuario.getNombreUsuario());
            logger.info("Preferencias recibidas: {}", preferencias);

            if (clienteRepository.existsById(usuario.getIdUsuario())) {
                logger.warn("Cliente ya existe para usuario ID: {}", usuario.getIdUsuario());
                return;
            }

            Cliente cliente = new Cliente();
            // Con @MapsId, NO debemos establecer el idUsuario manualmente
            // Solo establecemos el usuario y JPA manejará el ID automáticamente
            cliente.setUsuario(usuario);

            // Establecer preferencias con valor por defecto si es nulo o vacío
            String preferenciasFinal = (preferencias != null && !preferencias.isBlank())
                ? preferencias.trim()
                : "Sin Preferencias";
            cliente.setPreferencias(preferenciasFinal);
            cliente.setCalificacion(null);

            logger.info("Objeto Cliente creado en memoria");
            logger.info("Cliente - Preferencias finales: {}", cliente.getPreferencias());
            logger.info("Cliente - Usuario asociado: {}", cliente.getUsuario() != null ? "SI" : "NO");

            Cliente clienteGuardado = clienteRepository.saveAndFlush(cliente);

            logger.info("✓✓✓ Cliente guardado en BD exitosamente");
            logger.info("ID del cliente guardado: {}", clienteGuardado.getIdUsuario());
            logger.info("Preferencias guardadas: {}", clienteGuardado.getPreferencias());
            logger.info("=== FIN crearCliente EXITOSO ===");
        } catch (Exception e) {
            logger.error("=== ERROR EN crearCliente ===");
            logger.error("Tipo de error: {}", e.getClass().getName());
            logger.error("Mensaje: {}", e.getMessage());
            logger.error("Causa raíz: {}", e.getCause() != null ? e.getCause().getMessage() : "N/A");
            logger.error("Stack trace completo:", e);
            logger.error("=== FIN ERROR ===");
            logger.error("No se pudo crear el cliente, pero el usuario ya fue guardado");
        }
    }

    protected void crearProductorSinThrow(Usuario usuario, String tipoCultivo) {
        try {
            logger.info("=== INICIO crearProductor ===");
            logger.info("Usuario ID: {}", usuario.getIdUsuario());
            logger.info("Usuario nombre: {}", usuario.getNombreUsuario());
            logger.info("Tipo de cultivo: {}", tipoCultivo);

            if (productorRepository.existsById(usuario.getIdUsuario())) {
                logger.warn("Productor ya existe para usuario ID: {}", usuario.getIdUsuario());
                return;
            }

            Productor productor = new Productor();
            productor.setIdProductor(usuario.getIdUsuario());
            productor.setUsuario(usuario);
            productor.setCalificacion(null);

            if (tipoCultivo != null && !tipoCultivo.isBlank()) {
                try {
                    productor.setTipoCultivo(Productor.TipoCultivo.valueOf(tipoCultivo));
                    logger.info("Tipo de cultivo asignado: {}", tipoCultivo);
                } catch (IllegalArgumentException e) {
                    logger.warn("Tipo de cultivo no válido: {}, usando null", tipoCultivo);
                    productor.setTipoCultivo(null);
                }
            } else {
                logger.info("No se especificó tipo de cultivo");
            }

            Productor productorGuardado = productorRepository.saveAndFlush(productor);
            logger.info("✓✓✓ Productor guardado en BD exitosamente");
            logger.info("ID del productor guardado: {}", productorGuardado.getIdProductor());
            logger.info("=== FIN crearProductor EXITOSO ===");
        } catch (Exception e) {
            logger.error("=== ERROR EN crearProductor ===");
            logger.error("Tipo de error: {}", e.getClass().getName());
            logger.error("Mensaje: {}", e.getMessage());
            logger.error("Stack trace:", e);
            logger.error("=== FIN ERROR ===");
        }
    }

    protected void crearTransportistaSinThrow(Usuario usuario, String zonasEntrega) {
        try {
            logger.info("=== INICIO crearTransportista ===");
            logger.info("Usuario ID: {}", usuario.getIdUsuario());
            logger.info("Usuario nombre: {}", usuario.getNombreUsuario());
            logger.info("Zonas de entrega: {}", zonasEntrega);

            if (transportistaRepository.existsById(usuario.getIdUsuario())) {
                logger.warn("Transportista ya existe para usuario ID: {}", usuario.getIdUsuario());
                return;
            }

            Transportista transportista = new Transportista();
            transportista.setIdUsuario(usuario.getIdUsuario());
            transportista.setUsuario(usuario);
            transportista.setCalificacion(null);

            String zonasEntregaFinal = (zonasEntrega != null && !zonasEntrega.isBlank())
                ? zonasEntrega.trim()
                : "No especificado";
            transportista.setZonasEntrega(zonasEntregaFinal);

            Transportista transportistaGuardado = transportistaRepository.saveAndFlush(transportista);
            logger.info("✓✓✓ Transportista guardado en BD exitosamente");
            logger.info("ID del transportista guardado: {}", transportistaGuardado.getIdUsuario());
            logger.info("Zonas de entrega guardadas: {}", transportistaGuardado.getZonasEntrega());
            logger.info("=== FIN crearTransportista EXITOSO ===");
        } catch (Exception e) {
            logger.error("=== ERROR EN crearTransportista ===");
            logger.error("Tipo de error: {}", e.getClass().getName());
            logger.error("Mensaje: {}", e.getMessage());
            logger.error("Stack trace:", e);
            logger.error("=== FIN ERROR ===");
        }
    }

    protected void crearAsesorSinThrow(Usuario usuario, String tipoAsesoria) {
        try {
            logger.info("=== INICIO crearAsesor ===");
            logger.info("Usuario ID: {}", usuario.getIdUsuario());
            logger.info("Usuario nombre: {}", usuario.getNombreUsuario());
            logger.info("Tipo de asesoría: {}", tipoAsesoria);

            if (asesorRepository.existsById(usuario.getIdUsuario())) {
                logger.warn("Asesor ya existe para usuario ID: {}", usuario.getIdUsuario());
                return;
            }

            Asesor asesor = new Asesor();
            asesor.setIdUsuario(usuario.getIdUsuario());
            asesor.setUsuario(usuario);
            asesor.setCalificacion(null);

            String tipoAsesoriaFinal = (tipoAsesoria != null && !tipoAsesoria.isBlank())
                ? tipoAsesoria.trim()
                : "No especificado";
            asesor.setTipoAsesoria(tipoAsesoriaFinal);

            Asesor asesorGuardado = asesorRepository.saveAndFlush(asesor);
            logger.info("✓✓✓ Asesor guardado en BD exitosamente");
            logger.info("ID del asesor guardado: {}", asesorGuardado.getIdUsuario());
            logger.info("Tipo de asesoría guardada: {}", asesorGuardado.getTipoAsesoria());
            logger.info("=== FIN crearAsesor EXITOSO ===");
        } catch (Exception e) {
            logger.error("=== ERROR EN crearAsesor ===");
            logger.error("Tipo de error: {}", e.getClass().getName());
            logger.error("Mensaje: {}", e.getMessage());
            logger.error("Stack trace:", e);
            logger.error("=== FIN ERROR ===");
        }
    }
}
