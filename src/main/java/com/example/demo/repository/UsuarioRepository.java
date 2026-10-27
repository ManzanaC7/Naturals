package com.example.demo.repository;

import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // <--- AGREGA ESTA LÍNEA

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Este método busca al usuario validando ambos campos
    List<Usuario> findByNombreUsuarioAndContrasena(String nombreUsuario, String contrasena);
    
}