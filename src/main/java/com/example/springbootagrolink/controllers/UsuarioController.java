package com.example.springbootagrolink.controllers;



import com.example.springbootagrolink.model.Usuario;
import com.example.springbootagrolink.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/usuarios")
public class UsuarioController {


    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // GET - Obtener todos los usuarios
    @GetMapping
  public String obtenerTodosLosUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodosLosUsuarios());
        return "usuarios/lista"; // Nombre de la vista (HTML/JSP) que mostrará la lista de usuarios
    }

    // GET - Obtener usuario por ID
    @GetMapping("/{id}")
    public String obtenerUsuarioPorId(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id).orElse(null);
        model.addAttribute("usuario", usuario);
        return "usuarios/detalle"; // Nombre de la vista (HTML/JSP) que mostrará los detalles del usuario
    }

    // POST - Crear nuevo usuario
    @PostMapping("/crear")
    public String crearUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/crear"; // Nombre de la vista (HTML/JSP) para crear un nuevo usuario
    }

    // POST - Guardar nuevo usuario
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        usuarioService.crearUsuario(usuario);
        return "redirect:/usuarios"; // Redirigir a la lista de usuarios después de guardar
    }

    // PUT - Actualizar usuario
    @PostMapping("/{id}")
    public String actualizarUsuario(@PathVariable Integer id, @ModelAttribute Usuario usuario) {
        usuarioService.actualizarUsuario(id, usuario);
        return "redirect:/usuarios"; // Redirigir a la lista de usuarios después de actualizar
    }

    // DELETE - Eliminar usuario
    @GetMapping("/{id}")
    public String eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/usuarios"; // Redirigir a la lista de usuarios después de eliminar
    }
}
