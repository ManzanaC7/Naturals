package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@ModelAttribute Usuario usuario, HttpSession session, Model model) {
        List<Usuario> encontrados = usuarioRepository.findByNombreUsuarioAndContrasena(
            usuario.getNombreUsuario(), 
            usuario.getContrasena()
        );
        
        if (!encontrados.isEmpty()) {
            session.setAttribute("usuarioLogueado", encontrados.get(0));
            return "redirect:/inventario";
        }
        
        model.addAttribute("error", "Usuario o contraseña incorrectos");
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario) {
        usuarioRepository.save(usuario);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}