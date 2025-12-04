package com.example.springbootagrolink.services;

import com.example.springbootagrolink.repository.UsuarioRepository;
import com.example.springbootagrolink.model.Usuario;
import com.example.springbootagrolink.services.Idao.Idao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UsuarioService implements Idao<Usuario, Integer> {

    /**
     * Repositorio de Usuario - Interfaz que extiende JpaRepository
     *
     * final - Indica que esta variable no puede ser reasignada después de la inicialización.
     *         Es una buena práctica para las dependencias inyectadas.
     *
     * JpaRepository proporciona métodos predefinidos como:
     * - findAll(), findById(), save(), deleteById(), existsById(), count()
     */
    @Autowired
    private  UsuarioRepository usuarioRepository;



    /**
     * Implementación del método obtenerTodos() de la interfaz Idao
     *
     * @Override - Anotación que indica que este método sobrescribe/implementa
     *             un método de la interfaz padre. Ayuda a prevenir errores.
     *
     * @return List<Usuario> - Lista con todos los usuarios de la base de datos
     *
     * usuarioRepository.findAll() - Método de JpaRepository que ejecuta:
     *     SELECT * FROM tb_usuarios
     */
    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Implementación del método obtenerPorId() de la interfaz Idao
     *
     * @param id - ID del usuario a buscar
     * @return Optional<Usuario> - Contenedor que puede tener un Usuario o estar vacío
     *
     * Optional es una clase de Java 8+ que evita NullPointerException.
     * Métodos útiles de Optional:
     * - isPresent(): retorna true si hay valor
     * - get(): obtiene el valor (lanza excepción si está vacío)
     * - orElse(valorPorDefecto): retorna el valor o un default
     * - orElseThrow(): lanza excepción personalizada si está vacío
     *
     * usuarioRepository.findById(id) ejecuta:
     *     SELECT * FROM tb_usuarios WHERE id_usuario = ?
     */
    @Override
    public Optional<Usuario> obtenerPorId(Integer id) {
        return usuarioRepository.findById(id);
    }


    /**
     * Implementación del método guardar() de la interfaz Idao
     *
     * @param entidad - Objeto Usuario con los datos a guardar
     * @return Usuario - El usuario guardado con su ID generado
     *
     * usuarioRepository.save(entidad) realiza:
     * - Si el ID es null o no existe: INSERT INTO tb_usuarios (...)
     * - Si el ID existe: UPDATE tb_usuarios SET ... WHERE id_usuario = ?
     *
     * JPA automáticamente:
     * - Genera el ID (por @GeneratedValue en la entidad)
     * - Establece las fechas de auditoría (si usas @CreatedDate, @LastModifiedDate)
     * - Retorna la entidad con todos los campos actualizados
     */
    @Override
    public Usuario guardar(Usuario entidad) {
        return usuarioRepository.save(entidad);
    }

    /**
     * Implementación del método actualizar() de la interfaz Idao
     *
     * @param id - ID del usuario a actualizar
     * @param entidad - Objeto con los nuevos datos
     * @return Usuario actualizado, o null si no se encuentra
     *
     * Este método sigue el patrón:
     * 1. Buscar la entidad existente
     * 2. Verificar si existe
     * 3. Actualizar los campos necesarios
     * 4. Guardar los cambios
     *
     * IMPORTANTE: Aquí actualizas SOLO los campos que quieres modificar.
     * Puedes personalizar qué campos actualizar según tus necesidades.
     */
    @Override
    public Usuario actualizar(Integer id, Usuario entidad) {
        // Buscar el usuario existente en la base de datos
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);

        // Verificar si el usuario existe
        if (usuarioExistente.isPresent()) {
            // Obtener el usuario de dentro del Optional
            Usuario usuario = usuarioExistente.get();

            // Actualizar cada campo con los nuevos valores
            // Aquí puedes agregar validaciones adicionales si lo necesitas
            usuario.setNombre(entidad.getNombre());
            usuario.setNombreUsuario(entidad.getNombreUsuario());
            usuario.setContrasenaUsuario(entidad.getContrasenaUsuario());
            usuario.setApellido(entidad.getApellido());
            usuario.setCorreo(entidad.getCorreo());
            usuario.setCiudad(entidad.getCiudad());
            usuario.setDepartamento(entidad.getDepartamento());
            usuario.setDireccion(entidad.getDireccion());
            usuario.setCedula(entidad.getCedula());
            usuario.setTelefono(entidad.getTelefono());

            // Guardar los cambios en la base de datos
            return usuarioRepository.save(usuario);
        }

        // Si no existe, retornar null
        return null;
    }

    /**
     * Implementación del método eliminar() de la interfaz Idao
     *
     * @param id - ID del usuario a eliminar
     * @return true si se eliminó exitosamente, false si no existía
     *
     * Primero verifica que exista (para evitar excepciones) y luego elimina.
     *
     * usuarioRepository.existsById(id) ejecuta:
     *     SELECT COUNT(*) FROM tb_usuarios WHERE id_usuario = ?
     *
     * usuarioRepository.deleteById(id) ejecuta:
     *     DELETE FROM tb_usuarios WHERE id_usuario = ?
     */
    @Override
    public boolean eliminar(Integer id) {
        // Verificar si el usuario existe
        if (usuarioRepository.existsById(id)) {
            // Si existe, eliminarlo
            usuarioRepository.deleteById(id);
            return true;
        }
        // Si no existe, retornar false
        return false;
    }

    /**
     * Implementación del método existe() de la interfaz Idao
     *
     * @param id - ID del usuario a verificar
     * @return true si existe, false si no existe
     *
     * Útil para validaciones antes de realizar operaciones.
     * Por ejemplo, antes de actualizar, puedes verificar si existe.
     */

    /*
    @Override
    public boolean existe(Integer id) {
        return usuarioRepository.existsById(id);
    }
*/


    // ==================== MÉTODOS PERSONALIZADOS ====================
    // Aquí puedes agregar métodos específicos para Usuario que no están en Idao
    // Por ejemplo: buscarPorCorreo, buscarPorCedula, cambiarContrasena, etc.


    /**
     * Ejemplo de método personalizado: buscar usuario por correo
     * Este método NO está en la interfaz Idao, es específico de Usuario
     *
     * Para implementarlo, necesitarías agregar el método en UsuarioRepository:
     * Optional<Usuario> findByCorreo(String correo);
     */
    // public Optional<Usuario> buscarPorCorreo(String correo) {
    //     return usuarioRepository.findByCorreo(correo);
    // }
}
