package com.example.springbootagrolink.services.Idao;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica DAO (Data Access Object) que define las operaciones CRUD básicas
 *
 * @param <T> Tipo de entidad (modelo) con la que trabajará el DAO (por ejemplo: Usuario, Producto, etc.)
 * @param <ID> Tipo de dato del identificador de la entidad (por ejemplo: Integer, Long, String, etc.)
 *
 * Esta interfaz utiliza Generics de Java para ser reutilizable con cualquier entidad
 * y cualquier tipo de ID, evitando duplicación de código en los servicios.
 */
public interface Idao<T, ID> {

    /**
     * Obtiene todas las entidades de la base de datos
     *
     * @return Lista con todas las entidades encontradas. Si no hay datos, retorna una lista vacía.
     *
     * Ejemplo de uso:
     * List<Usuario> usuarios = usuarioService.obtenerTodos();
     */
    List<T> obtenerTodos();

    /**
     * Busca una entidad por su identificador único
     *
     * @param id Identificador de la entidad a buscar
     * @return Optional<T> que contiene la entidad si existe, o está vacío si no se encuentra
     *
     * Se usa Optional para evitar null pointer exceptions y manejar mejor los casos
     * donde la entidad no existe.
     *
     * Ejemplo de uso:
     * Optional<Usuario> usuario = usuarioService.obtenerPorId(1);
     * if(usuario.isPresent()) {
     *     // Hacer algo con usuario.get()
     * }
     */
    Optional<T> obtenerPorId(ID id);

    /**
     * Guarda una nueva entidad en la base de datos
     *
     * @param entidad Objeto con los datos de la nueva entidad a guardar
     * @return La entidad guardada, generalmente con su ID generado automáticamente
     *
     * En JPA, el método save() del repositorio se encarga de:
     * - Generar el ID automáticamente (si usa @GeneratedValue)
     * - Persistir la entidad en la base de datos
     * - Retornar la entidad con todos sus campos actualizados
     *
     * Ejemplo de uso:
     * Usuario nuevoUsuario = new Usuario();
     * nuevoUsuario.setNombre("Juan");
     * Usuario guardado = usuarioService.guardar(nuevoUsuario);
     */
    T guardar(T entidad);

    /**
     * Actualiza los datos de una entidad existente
     *
     * @param id Identificador de la entidad a actualizar
     * @param entidad Objeto con los nuevos datos para actualizar
     * @return La entidad actualizada, o null si no se encuentra
     *
     * Este método primero verifica que la entidad exista, luego actualiza
     * sus campos y finalmente guarda los cambios en la base de datos.
     *
     * Ejemplo de uso:
     * Usuario usuarioActualizado = new Usuario();
     * usuarioActualizado.setNombre("Juan Actualizado");
     * Usuario resultado = usuarioService.actualizar(1, usuarioActualizado);
     */
    T actualizar(ID id, T entidad);

    /**
     * Elimina una entidad de la base de datos por su ID
     *
     * @param id Identificador de la entidad a eliminar
     * @return true si la entidad fue eliminada exitosamente, false si no existía
     *
     * Antes de eliminar, verifica que la entidad exista para evitar errores.
     *
     * Ejemplo de uso:
     * boolean eliminado = usuarioService.eliminar(1);
     * if(eliminado) {
     *     System.out.println("Usuario eliminado correctamente");
     * }
     */
    boolean eliminar(ID id);

    /**
     * Verifica si existe una entidad con el ID especificado
     *
     * @param id Identificador de la entidad a verificar
     * @return true si la entidad existe, false en caso contrario
     *
     * Útil para validaciones antes de realizar otras operaciones.
     *
     * Ejemplo de uso:
     * if(usuarioService.existe(1)) {
     *     // Proceder con la operación
     * }
     */
    boolean existe(ID id);


}
