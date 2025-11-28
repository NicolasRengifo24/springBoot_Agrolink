package com.example.springbootagrolink.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_compras")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "cliente")
public class Compra implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Integer idCompra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "id_cliente",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_compras_cliente")
    )
    private Cliente cliente;

    @Column(name = "fecha_hora_compra", nullable = false)
    private LocalDateTime fechaHoraCompra = LocalDateTime.now();

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "impuestos", precision = 10, scale = 2, nullable = false)
    private BigDecimal impuestos = BigDecimal.ZERO;

    @Column(name = "valor_envio", precision = 10, scale = 2)
    private BigDecimal valorEnvio = BigDecimal.ZERO;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "direccion_entrega", length = 200)
    private String direccionEntrega;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;
}

