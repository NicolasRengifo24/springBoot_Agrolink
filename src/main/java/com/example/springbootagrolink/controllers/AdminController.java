package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final EnvioRepository envioRepository;
    private final CompraRepository compraRepository;
    private final TransportistaRepository transportistaRepository;
    private final ProductorRepository productorRepository;
    private final FincaRepository fincaRepository;

    public AdminController(UsuarioRepository usuarioRepository,
                           ProductoRepository productoRepository,
                           EnvioRepository envioRepository,
                           CompraRepository compraRepository,
                           TransportistaRepository transportistaRepository,
                           ProductorRepository productorRepository,
                           FincaRepository fincaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.envioRepository = envioRepository;
        this.compraRepository = compraRepository;
        this.transportistaRepository = transportistaRepository;
        this.productorRepository = productorRepository;
        this.fincaRepository = fincaRepository;
    }

    @GetMapping("/admin/index")
    public String index(Model model) {
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalProductos", productoRepository.count());
        model.addAttribute("totalEnvios", envioRepository.count());
        model.addAttribute("totalCompras", compraRepository.count());
        model.addAttribute("totalTransportistas", transportistaRepository.count());
        model.addAttribute("totalProductores", productorRepository.count());
        model.addAttribute("totalFincas", fincaRepository.count());
        return "admin/index";
    }
}

