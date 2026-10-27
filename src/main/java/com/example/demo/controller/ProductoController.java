package com.example.demo.controller;

import com.example.demo.model.Movimiento;
import com.example.demo.model.Producto;
import com.example.demo.model.Usuario;
import com.example.demo.repository.MovimientoRepository;
import com.example.demo.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @GetMapping({"/", "/inventario"})
    public String verInventario(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("producto", new Producto());
        model.addAttribute("productos", productoRepository.findByUsuario(usuario));
        return "index";
    }

    @GetMapping("/inventario/editar/{id}")
    public String cargarProductoParaEditar(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        Producto productoAEditar = productoRepository.findById(id).orElse(new Producto());
        model.addAttribute("producto", productoAEditar);
        model.addAttribute("productos", productoRepository.findByUsuario(usuario));
        return "index";
    }

    @PostMapping("/inventario/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Asignamos el usuario logueado al producto
        producto.setUsuario(usuario);

        boolean esNuevo = (producto.getId() == null);

        if (esNuevo) {
            productoRepository.save(producto);

            double costoTotalCompra = (producto.getPrecioCompra() != null ? producto.getPrecioCompra() : 0.0) 
                                    * (producto.getCantidad() != null ? producto.getCantidad() : 0.0);

            if (costoTotalCompra > 0) {
                Movimiento movimiento = new Movimiento();
                movimiento.setTipo("EGRESO");
                movimiento.setDescripcion("Compra stock: " + producto.getNombre() + " (Cant: " + producto.getCantidad() + ")");
                movimiento.setMonto(costoTotalCompra);
                movimiento.setFecha(LocalDateTime.now());
                movimiento.setUsuario(usuario); // <-- ¡Asignado al usuario actual!
                movimientoRepository.save(movimiento);
            }
        } else {
            Producto productoAnterior = productoRepository.findById(producto.getId()).orElse(null);
            
            double cantidadAntigua = (productoAnterior != null && productoAnterior.getCantidad() != null) ? productoAnterior.getCantidad() : 0.0;
            double cantidadNueva = (producto.getCantidad() != null) ? producto.getCantidad() : 0.0;

            productoRepository.save(producto);

            if (cantidadNueva > cantidadAntigua) {
                double cantidadComprada = cantidadNueva - cantidadAntigua;
                double precioCompra = (producto.getPrecioCompra() != null) ? producto.getPrecioCompra() : 0.0;
                double costoReabastecimiento = cantidadComprada * precioCompra;

                if (costoReabastecimiento > 0) {
                    Movimiento movimiento = new Movimiento();
                    movimiento.setTipo("EGRESO");
                    movimiento.setDescripcion("Reabastecimiento: " + producto.getNombre() + " (Cant: +" + cantidadComprada + ")");
                    movimiento.setMonto(costoReabastecimiento);
                    movimiento.setFecha(LocalDateTime.now());
                    movimiento.setUsuario(usuario); // <-- ¡Asignado al usuario actual!
                    movimientoRepository.save(movimiento);
                }
            }
        }

        return "redirect:/inventario";
    }

    @PostMapping("/inventario/lote")
    public String accionEnLote(@RequestParam(value = "ids", required = false) List<Long> ids, 
                               @RequestParam("accion") String accion,
                               HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        if (ids == null || ids.isEmpty()) {
            return "redirect:/inventario";
        }

        if ("eliminar".equals(accion)) {
            for (Long id : ids) {
                productoRepository.deleteById(id);
            }
        } else if ("vender".equals(accion)) {
            double montoTotalVenta = 0.0;
            StringBuilder descripcion = new StringBuilder("Venta masiva de: ");

            for (int i = 0; i < ids.size(); i++) {
                Long id = ids.get(i);
                Producto producto = productoRepository.findById(id).orElse(null);
                
                if (producto != null && producto.getCantidad() > 0) {
                    producto.setCantidad(producto.getCantidad() - 1);
                    productoRepository.save(producto);
                    
                    montoTotalVenta += (producto.getPrecioVenta() != null ? producto.getPrecioVenta() : 0.0);
                    descripcion.append(producto.getNombre());
                    if (i < ids.size() - 1) descripcion.append(", ");
                }
            }

            if (montoTotalVenta > 0) {
                Movimiento movimiento = new Movimiento();
                movimiento.setTipo("INGRESO");
                movimiento.setDescripcion(descripcion.toString());
                movimiento.setMonto(montoTotalVenta);
                movimiento.setFecha(LocalDateTime.now());
                movimiento.setUsuario(usuario); // <-- ¡Asignado al usuario actual!
                movimientoRepository.save(movimiento);
            }
        }

        return "redirect:/inventario";
    }

    @GetMapping("/inventario/vender/{id}")
    public String venderProducto(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto != null && producto.getCantidad() > 0) {
            producto.setCantidad(producto.getCantidad() - 1);
            productoRepository.save(producto);

            Movimiento movimiento = new Movimiento();
            movimiento.setTipo("INGRESO");
            movimiento.setDescripcion("Venta de: " + producto.getNombre());
            movimiento.setMonto(producto.getPrecioVenta());
            movimiento.setFecha(LocalDateTime.now());
            movimiento.setUsuario(usuario); // <-- ¡Asignado al usuario actual!
            movimientoRepository.save(movimiento);
        }
        return "redirect:/inventario";
    }

    @GetMapping("/inventario/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        productoRepository.deleteById(id);
        return "redirect:/inventario";
    }
}