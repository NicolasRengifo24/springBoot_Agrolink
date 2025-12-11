package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Compra;
import com.example.springbootagrolink.model.Usuario;
import com.example.springbootagrolink.model.Envio;
import com.example.springbootagrolink.services.CompraService;
import com.example.springbootagrolink.repository.UsuarioRepository;
import com.example.springbootagrolink.repository.EnvioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/pago")
public class PagoController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnvioRepository envioRepository;

    /**
     * Método que agrega el contador del carrito a todas las vistas automáticamente
     */
    @ModelAttribute("cartCount")
    public Integer getCartCount(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            return 0;
        }
        return carrito.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
    }

    /**
     * Método que agrega el nombre del usuario autenticado a todas las vistas automáticamente
     */
    @ModelAttribute("nombreUsuario")
    public String getNombreUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            Usuario usuario = usuarioRepository.findByNombreUsuario(username).orElse(null);
            if (usuario != null) {
                return usuario.getNombre();
            }
        }
        return null;
    }

    /**
     * Mostrar la pasarela de pago
     */
    @GetMapping("/index")
    public String mostrarPasarelaPago(
            @RequestParam(required = false) Integer idCompra,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // Si no se proporciona idCompra, intentar obtenerlo de la sesión
        if (idCompra == null) {
            idCompra = (Integer) session.getAttribute("idCompraActual");
        }

        if (idCompra == null) {
            redirectAttributes.addFlashAttribute("error", "No se encontró información de la compra");
            return "redirect:/cliente/carrito";
        }

        // Obtener la compra
        Compra compra = compraService.obtenerPorId(idCompra).orElse(null);

        if (compra == null) {
            redirectAttributes.addFlashAttribute("error", "Compra no encontrada");
            return "redirect:/cliente/carrito";
        }

        // Agregar la compra al modelo
        model.addAttribute("compra", compra);

        return "pago/index";
    }

    /**
     * Procesar el pago
     */
    @PostMapping("/procesar")
    public String procesarPago(
            @RequestParam Integer idCompra,
            @RequestParam String numeroTarjeta,
            @RequestParam String nombreTitular,
            @RequestParam String fechaExpiracion,
            @RequestParam String cvv,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Aquí iría la lógica real de procesamiento de pago con una pasarela de pago
            // Por ahora, solo simulamos un pago exitoso

            // Crear el envío asociado a la compra
            Compra compra = compraService.obtenerPorId(idCompra).orElse(null);

            if (compra != null) {
                Envio envio = new Envio();
                envio.setCompra(compra);
                envio.setDireccionDestino(compra.getDireccionEntrega());
                envio.setEstadoEnvio(Envio.EstadoEnvio.Buscando_Transporte);
                envio.setCostoTotal(compra.getValorEnvio());
                envioRepository.save(envio);
            }

            // Limpiar la sesión
            session.removeAttribute("idCompraActual");

            redirectAttributes.addFlashAttribute("success", "¡Pago procesado exitosamente! Tu pedido está en camino.");
            return "redirect:/pago/confirmacion?idCompra=" + idCompra;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
            return "redirect:/pago/index?idCompra=" + idCompra;
        }
    }

    /**
     * Página de confirmación de pago
     */
    @GetMapping("/confirmacion")
    public String confirmacionPago(
            @RequestParam Integer idCompra,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Compra compra = compraService.obtenerPorId(idCompra).orElse(null);

        if (compra == null) {
            redirectAttributes.addFlashAttribute("error", "Compra no encontrada");
            return "redirect:/";
        }

        model.addAttribute("compra", compra);
        return "pago/confirmacion";
    }
}

