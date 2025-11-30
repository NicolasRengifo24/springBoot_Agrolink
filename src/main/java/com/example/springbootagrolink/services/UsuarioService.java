package com.example.springbootagrolink.services;
import com.example.springbootagrolink.repository.UsuarioRepository;
import com.example.springbootagrolink.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {


    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    // Obtener todos los usuarios
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    // Obtener usuario por ID
    public Optional<Usuario> obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    // Crear nuevo usuario
    public Usuario crearUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Actualizar usuario
    public Usuario actualizarUsuario(Integer id, Usuario usuarioActualizado) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setNombreUsuario(usuarioActualizado.getNombreUsuario());
            usuario.setContrasenaUsuario(usuarioActualizado.getContrasenaUsuario());
            usuario.setApellido(usuarioActualizado.getApellido());
            usuario.setCorreo(usuarioActualizado.getCorreo());
            usuario.setCiudad(usuarioActualizado.getCiudad());
            usuario.setDepartamento(usuarioActualizado.getDepartamento());
            usuario.setDireccion(usuarioActualizado.getDireccion());
            usuario.setCedula(usuarioActualizado.getCedula());
            usuario.setTelefono(usuarioActualizado.getTelefono());
            return usuarioRepository.save(usuario);
        }
        return null;
    }


    // Eliminar usuario
    public boolean eliminarUsuario(Integer id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
