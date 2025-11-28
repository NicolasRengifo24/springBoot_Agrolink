package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(
    name = "tb_administradores",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_administradores_usuario", columnNames = "id_usuario")
    }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "usuario")
public class Administrador implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_administrador")
    private Integer idAdministrador;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_usuario",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_administradores_usuario")
    )
    private Usuario usuario;

    @Column(name = "privilegios_admin", length = 200)
    private String privilegiosAdmin;
}

