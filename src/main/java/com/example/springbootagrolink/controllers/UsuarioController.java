package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Usuario;
import com.example.springbootagrolink.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


/**
 * @RequestMapping("/usuarios") - Define la ruta base para todos los endpoints de este controlador.
 *                                  Todos los métodos heredan este prefijo.
 *                                  Ejemplo: /usuarios, /usuarios/crear, /usuarios/1, etc.
 *
 * La diferencia entre @Controller y @RestController:
 * - @Controller: Retorna vistas (HTML) usando un motor de plantillas (Thymeleaf, JSP, etc.)
 * - @RestController: Retorna datos (JSON, XML) directamente al cliente
 */


@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    /**
     * Servicio de Usuario que implementa Idao
     * Aquí usamos los métodos de la interfaz Idao:
     * - obtenerTodos(), obtenerPorId(), guardar(), actualizar(), eliminar(), etc.
     */
    @Autowired
    private  UsuarioService usuarioService;


    /**
     * GET /usuarios - Listar todos los usuarios
     *
     * @GetMapping - Maneja peticiones HTTP GET a /usuarios
     *
     * @param model - Objeto Model de Spring para pasar datos a la vista
     *                Model es como un "mapa" donde agregas datos que la vista podrá acceder
     *
     * @return String con el nombre de la vista (template) a renderizar
     *         "usuarios/lista" busca el archivo: templates/usuarios/lista.html
     *
     * Flujo:
     * 1. Cliente hace GET /usuarios
     * 2. Se ejecuta usuarioService.obtenerTodos() (método de Idao)
     * 3. Se agrega la lista al modelo con el nombre "usuarios"
     * 4. Se retorna la vista "usuarios/lista"
     * 5. Thymeleaf renderiza lista.html con los datos
     */
    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodos());
        return "usuarios/lista";
    }

    /**
     * GET /usuarios/{id} - Ver detalles de un usuario específico
     *
     * @PathVariable - Extrae el valor del ID de la URL
     *                 Ejemplo: GET /usuarios/5 → id = 5
     *
     * @param id - ID del usuario a buscar
     * @param model - Modelo para pasar datos a la vista
     *
     * @return Vista con los detalles del usuario
     *
     * usuarioService.obtenerPorId(id) retorna Optional<Usuario>
     * .orElse(null) extrae el Usuario o retorna null si no existe
     */
    @GetMapping("/{id}")
    public String verDetalleUsuario(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id).orElse(null);
        model.addAttribute("usuario", usuario);
        return "usuarios/detalle";
    }

    /**
     * GET /usuarios/nuevo - Mostrar formulario para crear un nuevo usuario
     *
     * Este método NO guarda nada, solo muestra el formulario vacío.
     *
     * @param model - Modelo para pasar datos a la vista
     *
     * @return Vista con el formulario de creación
     *
     * Se crea un nuevo Usuario() vacío y se pasa a la vista.
     * La vista usará este objeto para vincular los campos del formulario (th:object).
     *
     * Ejemplo en Thymeleaf:
     * <form th:action="@{/usuarios/guardar}" th:object="${usuario}" method="post">
     *     <input type="text" th:field="*{nombre}" />
     * </form>
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/crear";
    }

    /**
     * POST /usuarios/guardar - Guardar un nuevo usuario en la base de datos
     *
     * @PostMapping - Maneja peticiones HTTP POST (envío de formularios)
     *
     * @ModelAttribute - Vincula los datos del formulario al objeto Usuario
     *                   Spring automáticamente:
     *                   1. Crea un objeto Usuario
     *                   2. Llena sus campos con los datos del formulario
     *                   3. Lo pasa como parámetro al método
     *
     * @param usuario - Objeto Usuario con los datos del formulario
     *
     * @return Redirección a la lista de usuarios
     *
     * "redirect:/usuarios" - Redirige al navegador a GET /usuarios
     *                         Esto previene el problema de "reenviar formulario" si el usuario
     *                         recarga la página (patrón Post-Redirect-Get)
     *
     * IMPORTANTE: Aquí usamos guardar() de la interfaz Idao, NO crearUsuario()
     */
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        // Usar el método guardar() de la interfaz Idao
        usuarioService.guardar(usuario);
        return "redirect:/usuarios";
    }

    /**
     * GET /usuarios/editar/{id} - Mostrar formulario para editar un usuario existente
     *
     * @param id - ID del usuario a editar
     * @param model - Modelo para pasar datos a la vista
     *
     * @return Vista con el formulario de edición prellenado con los datos del usuario
     *
     * Similar a "nuevo", pero en lugar de un Usuario vacío, se carga uno existente.
     * La vista puede ser la misma que "crear" o una diferente según tu preferencia.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id).orElse(null);

        if (usuario == null) {
            // Si no existe, redirigir a la lista
            return "redirect:/usuarios";
        }

        model.addAttribute("usuario", usuario);
        return "usuarios/editar";
    }

    /**
     * POST /usuarios/actualizar/{id} - Actualizar un usuario existente
     *
     * @param id - ID del usuario a actualizar (viene de la URL)
     * @param usuario - Datos actualizados del formulario
     *
     * @return Redirección a la lista de usuarios
     *
     * IMPORTANTE: Aquí usamos actualizar() de la interfaz Idao
     *
     * El formulario HTML debe incluir el ID en la action:
     * <form th:action="@{/usuarios/actualizar/{id}(id=${usuario.idUsuario})}" method="post">
     */
    @PostMapping("/actualizar/{id}")
    public String actualizarUsuario(@PathVariable Integer id, @ModelAttribute Usuario usuario) {
        // Usar el método actualizar() de la interfaz Idao
        usuarioService.actualizar(id, usuario);
        return "redirect:/usuarios";
    }

    /**
     * GET /usuarios/eliminar/{id} - Eliminar un usuario
     *
     * @param id - ID del usuario a eliminar
     *
     * @return Redirección a la lista de usuarios
     *
     * IMPORTANTE: Aquí usamos eliminar() de la interfaz Idao
     *
     * NOTA: Idealmente DELETE debería ser un método HTTP DELETE, pero para simplificar
     * con vistas HTML (que solo soportan GET y POST nativamente), se usa GET.
     *
     * En un API REST puro, usarías:
     * @DeleteMapping("/{id}") con JavaScript/AJAX para hacer la petición DELETE.
     *
     * Desde la vista puedes llamarlo con un enlace:
     * <a th:href="@{/usuarios/eliminar/{id}(id=${usuario.idUsuario})}">Eliminar</a>
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id) {
        // Usar el método eliminar() de la interfaz Idao
        usuarioService.eliminar(id);
        return "redirect:/usuarios";
    }


}
