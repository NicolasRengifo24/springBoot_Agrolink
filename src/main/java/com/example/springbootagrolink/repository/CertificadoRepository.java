package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Integer> {
    // Listar certificados por asesor (id_usuario)
    List<Certificado> findByAsesor_IdUsuario(Integer asesorId);

    // Búsqueda opcional por rango de fechas de expedición
    @Query("SELECT c FROM Certificado c WHERE (:desde IS NULL OR c.fechaExpedicion >= :desde) AND (:hasta IS NULL OR c.fechaExpedicion <= :hasta)")
    List<Certificado> buscarPorRangoFechas(@Param("desde") LocalDate desde,
                                           @Param("hasta") LocalDate hasta);
}
