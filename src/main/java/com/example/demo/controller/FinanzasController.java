package com.example.demo.controller;

import com.example.demo.model.Movimiento;
import com.example.demo.model.Usuario;
import com.example.demo.repository.MovimientoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/finanzas")
public class FinanzasController {

    @Autowired
    private MovimientoRepository movimientoRepository;

    @GetMapping
    public String verFinanzas(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        // Obtenemos solo los movimientos de este usuario, ordenados del más reciente al más antiguo
        List<Movimiento> movimientos = movimientoRepository.findByUsuarioOrderByIdDesc(usuario);
        
        // Calcular totales de forma dinámica
        double totalIngresos = 0.0;
        double totalEgresos = 0.0;

        for (Movimiento m : movimientos) {
            if ("INGRESO".equalsIgnoreCase(m.getTipo())) {
                totalIngresos += (m.getMonto() != null ? m.getMonto() : 0.0);
            } else if ("EGRESO".equalsIgnoreCase(m.getTipo())) {
                totalEgresos += (m.getMonto() != null ? m.getMonto() : 0.0);
            }
        }

        double balanceTotal = totalIngresos - totalEgresos;

        // Mandar los datos a la vista HTML
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("totalEgresos", totalEgresos);
        model.addAttribute("balanceTotal", balanceTotal);
        model.addAttribute("usuarioLogueado", usuario);
        
        return "finanzas";
    }

    @PostMapping("/lote")
    public String accionEnLoteFinanzas(@RequestParam(value = "ids", required = false) List<Long> ids, 
                                       @RequestParam("accion") String accion, 
                                       HttpSession session) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        if (ids == null || ids.isEmpty()) {
            return "redirect:/finanzas";
        }

        if ("eliminar".equals(accion)) {
            for (Long id : ids) {
                movimientoRepository.deleteById(id);
            }
        }

        return "redirect:/finanzas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarMovimiento(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }
        
        movimientoRepository.deleteById(id);
        return "redirect:/finanzas";
    }
}