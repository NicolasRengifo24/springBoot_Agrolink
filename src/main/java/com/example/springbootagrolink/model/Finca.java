package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "tb_fincas")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"productor", "productoFincas"})
public class Finca implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_finca")
    private Integer idFinca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_usuario",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_fincas_productor")
    )
    private Productor productor;

    @Column(name = "nombre_finca", length = 100)
    private String nombreFinca;

    @Column(name = "direccion_finca", length = 200)
    private String direccionFinca;

    @Column(name = "certificado_BPA", length = 200)
    private String certificadoBPA = "Sin Certificado";

    @Column(name = "certificado_MIRFE", length = 200)
    private String certificadoMIRFE = "Sin Certificado";

    @Column(name = "certificado_MIPE", length = 200)
    private String certificadoMIPE = "Sin Certificado";

    @Column(name = "registro_ICA", length = 200)
    private String registroICA = "Sin Certificado";

    // Relación con ProductoFinca: una Finca puede tener muchas asociaciones ProductoFinca
    @OneToMany(mappedBy = "finca", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ProductoFinca> productoFincas;
}
