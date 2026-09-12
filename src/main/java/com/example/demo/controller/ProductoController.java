package com.example.demo.controller;

import com.example.demo.model.Movimiento;
import com.example.demo.model.Producto;
import com.example.demo.repository.MovimientoRepository;
import com.example.demo.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/inventario")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @GetMapping
    public String listarProductos(Model model, HttpSession session) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("producto", new Producto());
        model.addAttribute("usuarioLogueado", session.getAttribute("usuarioLogueado"));
        return "index";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, HttpSession session) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }
        
        productoRepository.save(producto);

        double precioVal = producto.getPrecio() != null ? producto.getPrecio() : 0.0;
        double cantidadVal = producto.getCantidad() != null ? producto.getCantidad() : 0.0;
        double costoTotal = precioVal * cantidadVal;
        
        if (costoTotal > 0) {
            Movimiento movimiento = new Movimiento("EGRESO", "Inversión/Compra en Inventario: " + producto.getNombre(), costoTotal);
            movimientoRepository.save(movimiento);
        }

        return "redirect:/inventario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }
        productoRepository.deleteById(id);
        return "redirect:/inventario";
    }
}