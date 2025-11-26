package com.example.springbootagrolink.model;

import jakarta.persistence.*;

import java.io.Serializable;


@Entity
@Table(name = "tb_usuarios")
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @Column(name = "contrasena_usuario", nullable = false, length = 100)
    private String contrasenaUsuario;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false, length = 50)
    private String ciudad;

    @Column(nullable = false, length = 50)
    private String departamento;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @Column(length = 15)
    private String telefono = "0000000000";

    public Usuario() {}

    public Usuario(Integer idUsuario, String nombre, String nombreUsuario, String contrasenaUsuario, String apellido, String correo, String ciudad, String departamento, String direccion, String cedula, String telefono) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.contrasenaUsuario = contrasenaUsuario;
        this.apellido = apellido;
        this.correo = correo;
        this.ciudad = ciudad;
        this.departamento = departamento;
        this.direccion = direccion;
        this.cedula = cedula;
        this.telefono = telefono;
    }

    // Getters y Setters
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContrasenaUsuario() { return contrasenaUsuario; }
    public void setContrasenaUsuario(String contrasenaUsuario) { this.contrasenaUsuario = contrasenaUsuario; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}

