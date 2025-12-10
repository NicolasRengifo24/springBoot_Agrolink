package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@Entity
@Table(name = "tb_usuarios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "nombre_usuario", nullable = false, length = 100, unique = true)
    private String nombreUsuario;

    @Column(name = "contrasena_usuario", nullable = false, length = 200)
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

    // ================== ROL DEL USUARIO ==================
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 50)
    private Rol rol = Rol.ROLE_CLIENTE;

    // ========== COORDENADAS GPS PARA GEOLOCALIZACIÓN ==========

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;
}
