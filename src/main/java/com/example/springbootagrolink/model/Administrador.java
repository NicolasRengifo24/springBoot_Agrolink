package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "tb_administradores")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"usuario"})
public class Administrador implements Serializable {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id_usuario", foreignKey = @ForeignKey(name = "fk_administradores_usuarios"))
    private Usuario usuario;

    @Column(name = "privilegios_admin", length = 200)
    private String privilegiosAdmin;
}
