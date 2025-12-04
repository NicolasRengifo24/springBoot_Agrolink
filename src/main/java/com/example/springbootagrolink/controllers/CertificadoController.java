package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Certificado;
import com.example.springbootagrolink.model.Asesor;
import com.example.springbootagrolink.services.CertificadoService;
import com.example.springbootagrolink.services.AsesorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Controller
@RequestMapping("/certificados")
public class CertificadoController {

    @Autowired
    private CertificadoService certificadoService;

    @Autowired
    private AsesorService asesorService;

    // Listar todos los certificados
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("certificados", certificadoService.obtenerTodos());
        return "certificados/lista";
    }

    // Formulario de nuevo certificado
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("certificado", new Certificado());
        model.addAttribute("asesores", asesorService.obtenerTodosLosAsesores());
        return "certificados/crear";
    }

    // Guardar nuevo certificado
    @PostMapping
    public String guardar(@ModelAttribute Certificado certificado,
                          @RequestParam Integer asesorId,
                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaExpedicion) {
        Asesor asesor = asesorService.obtenerAsesorPorId(asesorId)
                .orElseThrow(() -> new RuntimeException("Asesor no encontrado con ID: " + asesorId));
        certificado.setAsesor(asesor);
        certificado.setFechaExpedicion(fechaExpedicion);
        certificadoService.guardar(certificado);
        return "redirect:/certificados";
    }

    // Formulario de edición
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Certificado cert = certificadoService.obtenerPorId(id).orElse(null);
        if (cert == null) return "redirect:/certificados";
        model.addAttribute("certificado", cert);
        model.addAttribute("asesores", asesorService.obtenerTodosLosAsesores());
        return "certificados/editar";
    }

    // Actualizar certificado
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Integer id,
                             @ModelAttribute Certificado certificado,
                             @RequestParam(required = false) Integer asesorId,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaExpedicion) {
        if (asesorId != null) {
            Asesor asesor = asesorService.obtenerAsesorPorId(asesorId)
                    .orElseThrow(() -> new RuntimeException("Asesor no encontrado con ID: " + asesorId));
            certificado.setAsesor(asesor);
        }
        if (fechaExpedicion != null) {
            certificado.setFechaExpedicion(fechaExpedicion);
        }
        certificadoService.actualizar(id, certificado);
        return "redirect:/certificados";
    }

    // Eliminar certificado
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        certificadoService.eliminar(id);
        return "redirect:/certificados";
    }

    // Listar por asesor
    @GetMapping("/asesor/{asesorId}")
    public String listarPorAsesor(@PathVariable Integer asesorId, Model model) {
        model.addAttribute("certificados", certificadoService.obtenerPorAsesor(asesorId));
        return "certificados/lista";
    }
}
