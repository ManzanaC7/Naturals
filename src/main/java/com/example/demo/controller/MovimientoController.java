package com.example.demo.controller;

import com.example.demo.model.Movimiento;
import com.example.demo.repository.MovimientoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MovimientoController {

    @Autowired
    private MovimientoRepository movimientoRepository;

    @GetMapping("/finanzas")
    public String verFinanzas(HttpSession session, Model model) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        List<Movimiento> movimientos = movimientoRepository.findAll();

        // Calcular totales de forma segura
        double totalIngresos = movimientos.stream()
                .filter(m -> "INGRESO".equalsIgnoreCase(m.getTipo()))
                .mapToDouble(Movimiento::getMonto)
                .sum();

        double totalEgresos = movimientos.stream()
                .filter(m -> "EGRESO".equalsIgnoreCase(m.getTipo()))
                .mapToDouble(Movimiento::getMonto)
                .sum();

        double balance = totalIngresos - totalEgresos;

        model.addAttribute("movimientos", movimientos);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("totalEgresos", totalEgresos);
        model.addAttribute("balance", balance);
        model.addAttribute("usuarioLogueado", session.getAttribute("usuarioLogueado"));

        return "finanzas";
    }
}