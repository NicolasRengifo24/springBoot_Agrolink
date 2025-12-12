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

    // Mostrar formulario web para crear finca
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("finca", new Finca());
        return "finca/crear";
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
