package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.*;
import com.example.springbootagrolink.services.*;
import com.example.springbootagrolink.services.CategoriaProductoService;
import com.example.springbootagrolink.services.ServicioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/perfil")
public class perfilController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductorService productorService;

    @Autowired
    private AsesorService asesorService;

    @Autowired
    private TransportistaService transportistaService;

    @Autowired
    private CategoriaProductoService categoriaProductoService;

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private com.example.springbootagrolink.repository.UsuarioRepository usuarioRepository;

    // Servicios y repositorios disponibles que usaremos para estadísticas básicas
    @Autowired
    private ProductoService productoService;

    @Autowired
    private CompraService compraService;

    // Usamos el mismo ClienteService inyectado arriba: clienteService

    @GetMapping
    public String mostrarPerfil(Model model,
                                @AuthenticationPrincipal UserDetails userDetails,
                                HttpSession session) {

        // Validaciones básicas: si no hay userDetails, redirigimos al login
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Obtenemos el usuario por el nombre de usuario (UserDetails.getUsername())
        Usuario usuario = usuarioService.obtenerPorNombreUsuario(userDetails.getUsername());
        if (usuario == null) {
            // Si no encontramos el usuario en BBDD, redirigimos al login para evitar NPE
            return "redirect:/login";
        }
        // Si el usuario es cliente, redirigimos a la ruta específica del cliente
        if (usuario.getRol() != null && "ROLE_CLIENTE".equals(usuario.getRol().name())) {
            return "redirect:/cliente/perfil";
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("title", "Mi Perfil - " + getRolDisplayName(usuario.getRol()));

        String rol = usuario.getRol().name();
        String template = "";
        Object datosEspecificos = null;

        switch(rol) {
            case "ROLE_CLIENTE":
                // Buscar cliente por usuario (ClienteRepository.findByUsuario)
                datosEspecificos = clienteService.obtenerPorUsuario(usuario);
                template = "cliente/perfil :: cliente-perfil";
                break;
            case "ROLE_PRODUCTOR":
                // ProductorService tiene método obtenerPorNombreUsuario
                datosEspecificos = productorService.obtenerPorNombreUsuario(usuario.getNombreUsuario()).orElse(null);
                template = "productor/perfil :: productor-perfil";
                break;
            case "ROLE_ADMINISTRADOR":
                // No hay fragment específico por ahora
                datosEspecificos = null;
                template = "";
                break;
            case "ROLE_ASESOR":
                // No implementamos vista index para asesor ahora; cargamos el asesor si existe
                datosEspecificos = asesorService.obtenerAsesorPorId(usuario.getIdUsuario()).orElse(null);
                template = "asesor/perfil :: asesor-perfil";
                break;
            case "ROLE_TRANSPORTISTA":
                // Transportista se mapea por usuario.id en la entidad; intentamos obtenerlo por id
                datosEspecificos = transportistaService.obtenerPorId(usuario.getIdUsuario()).orElse(null);
                template = "transportista/perfil :: transportista-perfil";
                break;
        }

        // Añadir al modelo el atributo con el nombre que espera el fragment
        if ("ROLE_CLIENTE".equals(rol)) {
            model.addAttribute("cliente", datosEspecificos);
        } else if ("ROLE_PRODUCTOR".equals(rol)) {
            model.addAttribute("productor", datosEspecificos);
        } else if ("ROLE_TRANSPORTISTA".equals(rol)) {
            model.addAttribute("transportista", datosEspecificos);
        } else if ("ROLE_ASESOR".equals(rol)) {
            model.addAttribute("asesor", datosEspecificos);
        }

        model.addAttribute("datosEspecificos", datosEspecificos);
        model.addAttribute("templateEspecifico", template);

        // Agregar datos para el navbar (categorías, servicios)
        try {
            model.addAttribute("categorias", categoriaProductoService.obtenerTodos());
            Map<String, List<com.example.springbootagrolink.model.Servicio>> categoriasServicios = categorizarServicios(servicioService.obtenerTodosLosServicios());
            model.addAttribute("categoriasServicios", categoriasServicios);
        } catch (Exception e) {
            model.addAttribute("categorias", new java.util.ArrayList<>());
            model.addAttribute("categoriasServicios", new java.util.HashMap<>());
        }

        // Calcular cartCount desde sesión (si existe)
        int cartCount = 0;
        Object carritoObj = session.getAttribute("carrito");
        if (carritoObj instanceof java.util.Map) {
            try {
                @SuppressWarnings("unchecked")
                java.util.Map<Integer, Integer> carrito = (java.util.Map<Integer, Integer>) carritoObj;
                cartCount = carrito.values().stream().mapToInt(Integer::intValue).sum();
            } catch (Exception ignored) {}
        }
        model.addAttribute("cartCount", cartCount);

        // Flags para indicar si se debe mostrar el fragmento específico (evitar evaluaciones complejas en la plantilla)
        boolean mostrarCliente = "ROLE_CLIENTE".equals(rol) && datosEspecificos != null;
        boolean mostrarProductor = "ROLE_PRODUCTOR".equals(rol) && datosEspecificos != null;
        boolean mostrarAsesor = "ROLE_ASESOR".equals(rol) && datosEspecificos != null;
        boolean mostrarTransportista = "ROLE_TRANSPORTISTA".equals(rol) && datosEspecificos != null;

        model.addAttribute("mostrarCliente", mostrarCliente);
        model.addAttribute("mostrarProductor", mostrarProductor);
        model.addAttribute("mostrarAsesor", mostrarAsesor);
        model.addAttribute("mostrarTransportista", mostrarTransportista);

        // Agregar estadísticas según rol
        agregarEstadisticas(model, usuario, datosEspecificos);

        // usamos el layout específico para perfil que carga los fragments
        return "layouts/perfil";
    }

    // POST handler para actualizar datos básicos del usuario (perfil)
    @PostMapping("/editar")
    public String editarPerfil(@RequestParam(value = "idCliente", required = false) Integer idCliente,
                               @RequestParam String nombre,
                               @RequestParam String apellido,
                               @RequestParam String correo,
                               @RequestParam(required = false) String telefono,
                               @RequestParam(required = false) String cedula,
                               @RequestParam(required = false) String direccion,
                               @RequestParam(required = false) String preferencias,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            // Verificar autenticación
            if (userDetails == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Debe iniciar sesión para editar el perfil");
                return "redirect:/login";
            }

            // Si se proporcionó idCliente, trabajamos sobre el Cliente vinculado
            if (idCliente == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "ID de cliente no proporcionado");
                return "redirect:/perfil";
            }

            java.util.Optional<com.example.springbootagrolink.model.Cliente> clienteOpt = clienteService.obtenerClientePorId(idCliente);
            if (clienteOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado");
                return "redirect:/perfil";
            }

            com.example.springbootagrolink.model.Cliente cliente = clienteOpt.get();
            com.example.springbootagrolink.model.Usuario u = cliente.getUsuario();

            // Verificar que el usuario autenticado sea el propietario del perfil
            com.example.springbootagrolink.model.Usuario logged = usuarioService.obtenerPorNombreUsuario(userDetails.getUsername());
            if (logged == null || !logged.getIdUsuario().equals(u.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("errorMessage", "No tiene permisos para editar este perfil");
                return "redirect:/perfil";
            }

            // Actualizar campos en Usuario
            u.setNombre(nombre != null ? nombre : u.getNombre());
            u.setApellido(apellido != null ? apellido : u.getApellido());
            u.setCorreo(correo != null ? correo : u.getCorreo());
            if (telefono != null) u.setTelefono(telefono);
            if (direccion != null) u.setDireccion(direccion);
            if (cedula != null) u.setCedula(cedula);

            usuarioRepository.save(u);

            // Actualizar preferencias en Cliente si se envió
            if (preferencias != null) {
                cliente.setPreferencias(preferencias);
                clienteService.actualizarCliente(cliente.getIdUsuario(), cliente);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
        }
        return "redirect:/perfil";
    }

    private String getRolDisplayName(Rol rol) {
        switch(rol.name()) {
            case "ROLE_CLIENTE": return "Cliente";
            case "ROLE_PRODUCTOR": return "Productor";
            case "ROLE_ADMINISTRADOR": return "Administrador";
            case "ROLE_ASESOR": return "Asesor";
            case "ROLE_TRANSPORTISTA": return "Transportista";
            default: return "Usuario";
        }
    }

    private void agregarEstadisticas(Model model, Usuario usuario, Object datosEspecificos) {
        Map<String, Object> estadisticas = new HashMap<>();

        switch(usuario.getRol().name()) {
            case "ROLE_CLIENTE":
                if (datosEspecificos instanceof Cliente) {
                    Cliente cliente = (Cliente) datosEspecificos;
                    // Usar CompraService para contar compras realizadas por el cliente
                    int pedidos = compraService.contarPorCliente(cliente);
                    estadisticas.put("pedidosRealizados", pedidos);
                    estadisticas.put("comentariosRealizados", 0);
                    estadisticas.put("productosFavoritos", 0);
                }
                break;

            case "ROLE_PRODUCTOR":
                if (datosEspecificos instanceof Productor) {
                    Productor productor = (Productor) datosEspecificos;
                    // ProductoService ofrece métodos para obtener productos por productor
                    int productosActivos = productoService.obtenerPorProductor(productor.getIdProductor()).size();
                    estadisticas.put("productosActivos", productosActivos);
                    estadisticas.put("ventasMes", 0);
                    estadisticas.put("clientesAtendidos", 0);
                }
                break;

            case "ROLE_TRANSPORTISTA":
                // No implementado: devolvemos defaults si no hay datos específicos
                estadisticas.put("entregasCompletadas", 0);
                estadisticas.put("entregasPendientes", 0);
                estadisticas.put("entregasMes", 0);
                estadisticas.put("tasaExito", 0);
                estadisticas.put("tiempoPromedio", 0);
                estadisticas.put("saldoDisponible", 0);
                estadisticas.put("ingresosMes", 0);
                estadisticas.put("pendienteCobro", 0);
                break;

            case "ROLE_ASESOR":
                estadisticas.put("asesoriasCompletadas", 0);
                estadisticas.put("asesoriasActivas", 0);
                estadisticas.put("asesoriasMes", 0);
                estadisticas.put("tasaSatisfaccion", 0);
                estadisticas.put("clientesAtendidos", 0);
                estadisticas.put("ingresosMes", 0);
                break;
        }

        model.addAttribute("estadisticas", estadisticas);
    }

    // Helper to group servicios by category key (copiado simplificado)
    private Map<String, List<com.example.springbootagrolink.model.Servicio>> categorizarServicios(List<com.example.springbootagrolink.model.Servicio> servicios) {
        Map<String, List<com.example.springbootagrolink.model.Servicio>> map = new java.util.HashMap<>();
        if (servicios == null) return map;
        for (com.example.springbootagrolink.model.Servicio s : servicios) {
            String key = "Otros";
            try {
                if (s.getAsesor() != null && s.getAsesor().getTipoAsesoria() != null && !s.getAsesor().getTipoAsesoria().trim().isEmpty()) {
                    key = s.getAsesor().getTipoAsesoria();
                }
            } catch (Exception ignored) {
            }
            map.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(s);
        }
        return map;
    }
}