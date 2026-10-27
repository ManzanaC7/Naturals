package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Double precioCompra; // Costo del proveedor (genera egreso)
    private Double precioVenta;  // Precio al público (genera ingreso con ganancia)
    private Double cantidad;

    // Relación ManyToOne: Muchos productos pueden pertenecer a un Usuario
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Producto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(Double precioCompra) { this.precioCompra = precioCompra; }

    public Double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Double precioVenta) { this.precioVenta = precioVenta; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}