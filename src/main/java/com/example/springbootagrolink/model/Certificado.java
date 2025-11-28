package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "tb_certificados")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "asesor")
public class Certificado implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_certificado")
    private Integer idCertificado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_usuario",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_certificados_asesor")
    )
    private Asesor asesor;

    @Column(name = "tipo_certificado", length = 100, nullable = false)
    private String tipoCertificado;

    @Column(name = "descripcion_cert", length = 255, nullable = false)
    private String descripcionCert;

    @Column(name = "fecha_expedicion", nullable = false)
    private LocalDate fechaExpedicion;
}

