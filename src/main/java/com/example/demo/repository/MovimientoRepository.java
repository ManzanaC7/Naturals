package com.example.demo.repository;

import com.example.demo.model.Movimiento;
import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    // Busca los movimientos de un usuario ordenados por ID de forma descendente (del más nuevo al más viejo)
    List<Movimiento> findByUsuarioOrderByIdDesc(Usuario usuario);
}