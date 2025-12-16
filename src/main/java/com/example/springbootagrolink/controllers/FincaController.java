package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Finca;
import com.example.springbootagrolink.model.Productor;
import com.example.springbootagrolink.services.FincaService;
import com.example.springbootagrolink.services.ProductorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Controller
@RequestMapping("/fincas")
public class FincaController {

    @Autowired
    private FincaService fincaService;

    @Autowired
    private ProductorService productorService;

    // GET - Obtener todas las fincas
    @GetMapping
    public ResponseEntity<List<Finca>> obtenerTodos() {
        return ResponseEntity.ok(fincaService.obtenerTodos());
    }

    // GET - Obtener finca por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        return fincaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST - Crear nueva finca
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Finca finca) {
        try {
            Finca creada = fincaService.guardar(finca);
            return ResponseEntity.ok(creada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // PUT - Actualizar finca existente
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Finca finca) {
        try {
            Finca actualizada = fincaService.actualizar(id, finca);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // DELETE - Eliminar finca
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        if (fincaService.eliminar(id)) {
            return ResponseEntity.ok("Finca eliminada correctamente");
        }
        return ResponseEntity.status(404).body("No se encontró la finca con ID: " + id);
    }

    // GET - Obtener fincas por ID de productor
    @GetMapping("/productor/{productorId}")
    public ResponseEntity<List<Finca>> obtenerFincasPorProductor(@PathVariable Integer productorId) {
        return ResponseEntity.ok(fincaService.obtenerFincasPorProductor(productorId));
    }

    // PUT - Asignar productor a finca
    @PutMapping("/{fincaId}/asignar-productor/{productorId}")
    public ResponseEntity<?> asignarProductor(
            @PathVariable Integer fincaId,
            @PathVariable Integer productorId) {

        try {
            Finca fincaActualizada = fincaService.asignarProductor(fincaId, productorId);
            return ResponseEntity.ok(fincaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Mostrar lista web de fincas
    @GetMapping("/lista")
    public String mostrarListaFincas(Model model) {
        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;

        List<Finca> fincas;
        if (username != null) {
            Optional<Productor> productorOpt = productorService.obtenerPorNombreUsuario(username);
            if (productorOpt.isPresent()) {
                Productor productor = productorOpt.get();
                fincas = fincaService.obtenerPorProductor(productor.getIdProductor());
                model.addAttribute("productorLogueado", productor);
            } else {
                // Si no se encuentra productor asociado al usuario autenticado, retornar lista vacía
                fincas = Collections.emptyList();
            }
        } else {
            // Usuario no autenticado -> mostrar lista vacía
            fincas = Collections.emptyList();
        }

        model.addAttribute("fincas", fincas);
        return "fincas/lista";
    }

    // Mostrar detalle web de finca
    @GetMapping("/ver/{id}")
    public String mostrarVerFinca(@PathVariable Integer id, Model model) {
        fincaService.obtenerPorId(id).ifPresentOrElse(
            f -> model.addAttribute("finca", f),
            () -> model.addAttribute("finca", null)
        );
        return "fincas/ver";
    }

    // Mostrar formulario web para editar finca
    @GetMapping("/editar/{id}")
    public String mostrarEditarFinca(@PathVariable Integer id, Model model) {
        fincaService.obtenerPorId(id).ifPresentOrElse(
            f -> model.addAttribute("finca", f),
            () -> model.addAttribute("finca", null)
        );
        return "fincas/editar";
    }

    // Eliminar via GET (usado por botones en UI) y redirigir a la lista
    @GetMapping("/eliminar/{id}")
    public String eliminarFincaWeb(@PathVariable Integer id) {
        try {
            fincaService.eliminar(id);
        } catch (Exception e) {
            // log si es necesario
        }
        return "redirect:/fincas/lista";
    }

    // Mostrar formulario web para crear finca
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("finca", new Finca());
        return "fincas/crear";
    }

    // Procesar formulario web para crear finca
    @PostMapping("/crear")
    public String guardarFincaWeb(@ModelAttribute Finca finca) {
        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        if (username != null) {
            productorService.obtenerPorNombreUsuario(username).ifPresent(finca::setProductor);
        }
        fincaService.guardar(finca);
        return "redirect:/productos/crear";
    }
}
