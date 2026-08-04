package edu.university.system.model;

import java.math.BigDecimal;

/**
 * Catalogo de puestos o responsabilidades laborales asignables a empleados.
 * Conserva una descripcion opcional y un salario base de referencia para el
 * puesto sin reemplazar el salario individual del empleado.
 */
public class Cargo {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal salarioBase;

    public Cargo() {
        this.salarioBase = BigDecimal.ZERO;
    }

    public Cargo(Long id, String nombre, String descripcion) {
        this(id, nombre, descripcion, BigDecimal.ZERO);
    }

    public Cargo(Long id, String nombre, String descripcion, BigDecimal salarioBase) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.salarioBase = salarioBase == null ? BigDecimal.ZERO : salarioBase;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getSalarioBase() {
        return salarioBase;
    }

    public void setSalarioBase(BigDecimal salarioBase) {
        this.salarioBase = salarioBase == null ? BigDecimal.ZERO : salarioBase;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Cargo cargo)) {
            return false;
        }
        return id != null && id.equals(cargo.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}
