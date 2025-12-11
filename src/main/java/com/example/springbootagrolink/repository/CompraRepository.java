package com.example.springbootagrolink.repository;

import com.example.springbootagrolink.model.Compra;
import com.example.springbootagrolink.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer>, JpaSpecificationExecutor<Compra> {

    /**
     * Encuentra todas las compras de un cliente específico
     * @param cliente El cliente del que se quieren obtener las compras
     * @return Lista de compras del cliente ordenadas por fecha descendente
     */
    List<Compra> findByClienteOrderByFechaHoraCompraDesc(Cliente cliente);

    /**
     * Encuentra todas las compras de un cliente específico
     * @param cliente El cliente del que se quieren obtener las compras
     * @return Lista de compras del cliente
     */
    List<Compra> findByCliente(Cliente cliente);
}


