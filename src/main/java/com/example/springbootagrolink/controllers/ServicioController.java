package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Servicio;
import com.example.springbootagrolink.services.ServicioService;
import com.example.springbootagrolink.services.AsesorService;
import com.example.springbootagrolink.services.CategoriaProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("/servicios")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private AsesorService asesorService;

    @Autowired
    private CategoriaProductoService categoriaProductoService; // reutilizamos para el navbar

    // Exponer contador del carrito a la vista (como hace ClienteController)
    @ModelAttribute("cartCount")
    public Integer getCartCount(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            return 0;
        }
        return carrito.values().stream().mapToInt(Integer::intValue).sum();
    }

    // GET - Listar todos los servicios y mostrar la vista Servicios/Index
    @GetMapping
    public String obtenerTodosLosServicios(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "ubicacion", required = false) String ubicacion,
            @RequestParam(value = "precioMin", required = false) Double precioMin,
            @RequestParam(value = "precioMax", required = false) Double precioMax,
            @RequestParam(value = "soloDisponibles", required = false) Boolean soloDisponibles,
            Model model) {

        List<Servicio> servicios = servicioService.obtenerTodosLosServicios();

        // Para compatibilidad con la plantilla: categorías del navbar y mapa de categorías de servicios
        List<?> categorias = categoriaProductoService.obtenerTodos();
        Map<String, List<Servicio>> categoriasServicios = categorizarServicios(servicios);

        model.addAttribute("servicios", servicios);
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoriasServicios", categoriasServicios);

        // Mantener parámetros de búsqueda en la vista (si aplica)
        model.addAttribute("q", q);
        // Intentar parsear categoria a Integer si viene un id; sino dejar el string (slug)
        try {
            if (categoria != null) {
                Integer catId = Integer.valueOf(categoria);
                model.addAttribute("categoria", catId);
            } else {
                model.addAttribute("categoria", null);
            }
        } catch (NumberFormatException ex) {
            // no es numérico, mantener el slug para enlaces rápidos
            model.addAttribute("categoria", categoria);
        }
        model.addAttribute("ubicacion", ubicacion);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("soloDisponibles", soloDisponibles);

        return "Servicios/Index";
    }

    // GET - Obtener servicio por ID
    @GetMapping("/{id}")
    public String obtenerServicioPorId(@PathVariable Integer id, Model model) {
        Servicio servicio = servicioService.obtenerServicioPorId(id).orElse(null);
        model.addAttribute("servicio", servicio);
        return "servicios/detalle";
    }

    // GET - Mostrar formulario crear servicio
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("servicio", new Servicio());
        model.addAttribute("asesores", asesorService.obtenerTodosLosAsesores());
        return "servicios/crear";
    }

    // POST - Guardar servicio nuevo
    @PostMapping("/guardar")
    public String crearServicio(@ModelAttribute Servicio servicio) {
        servicioService.crearServicio(servicio);
        return "redirect:/servicios";
    }

    // POST - Actualizar servicio
    @PostMapping("/{id}")
    public String actualizarServicio(@PathVariable Integer id, @ModelAttribute Servicio servicio) {
        servicioService.actualizarServicio(id, servicio);
        return "redirect:/servicios";
    }

    // GET - Eliminar servicio
    @GetMapping("/eliminar/{id}")
    public String eliminarServicio(@PathVariable Integer id) {
        servicioService.eliminarServicio(id);
        return "redirect:/servicios";
    }

    // Copia de la función de categorización usada por ClienteController para popular el navbar de servicios
    private Map<String, List<Servicio>> categorizarServicios(List<Servicio> servicios) {
        Map<String, List<Servicio>> categoriasServicios = new HashMap<>();

        categoriasServicios.put("Veterinaria", new ArrayList<>());
        categoriasServicios.put("Asesoría Agrícola", new ArrayList<>());
        categoriasServicios.put("Transporte", new ArrayList<>());
        categoriasServicios.put("Capacitación", new ArrayList<>());
        categoriasServicios.put("Maquinaria", new ArrayList<>());
        categoriasServicios.put("Otros", new ArrayList<>());

        for (Servicio servicio : servicios) {
            if (servicio.getDescripcion() != null /* && servicio.getEstado() == Servicio.EstadoServicio.Activo */) {
                String descripcion = servicio.getDescripcion().toLowerCase();

                if (descripcion.contains("veterinari") || descripcion.contains("animal") ||
                    descripcion.contains("mascota") || descripcion.contains("ganad")) {
                    categoriasServicios.get("Veterinaria").add(servicio);
                } else if (descripcion.contains("asesor") || descripcion.contains("agricul") ||
                           descripcion.contains("cultivo") || descripcion.contains("siembra")) {
                    categoriasServicios.get("Asesoría Agrícola").add(servicio);
                } else if (descripcion.contains("transport") || descripcion.contains("envío") ||
                           descripcion.contains("logística") || descripcion.contains("entrega")) {
                    categoriasServicios.get("Transporte").add(servicio);
                } else if (descripcion.contains("capacita") || descripcion.contains("curso") ||
                           descripcion.contains("entrena") || descripcion.contains("taller")) {
                    categoriasServicios.get("Capacitación").add(servicio);
                } else if (descripcion.contains("maquina") || descripcion.contains("tracto") ||
                           descripcion.contains("mecanica") || descripcion.contains("motor")) {
                    categoriasServicios.get("Maquinaria").add(servicio);
                } else {
                    categoriasServicios.get("Otros").add(servicio);
                }
            }
        }

        categoriasServicios.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        return categoriasServicios;
    }
}
