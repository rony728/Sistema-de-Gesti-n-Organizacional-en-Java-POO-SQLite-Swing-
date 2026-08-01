package edu.university.system.model;

import java.math.BigDecimal;

/**
 * Departamento interno de una empresa. Representa una unidad organizacional
 * como Recursos Humanos, Finanzas, Tecnologia u Operaciones.
 */
public class Departamento {

    private Long id;
    private Empresa empresa;
    private String nombre;
    private BigDecimal presupuesto;
    private boolean activo;

    public Departamento() {
        this.presupuesto = BigDecimal.ZERO;
        this.activo = true;
    }

    public Departamento(Long id, Empresa empresa, String nombre, BigDecimal presupuesto) {
        this(id, empresa, nombre, presupuesto, true);
    }

    public Departamento(Long id, Empresa empresa, String nombre, BigDecimal presupuesto, boolean activo) {
        this.id = id;
        this.empresa = empresa;
        this.nombre = nombre;
        this.presupuesto = presupuesto == null ? BigDecimal.ZERO : presupuesto;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(BigDecimal presupuesto) {
        this.presupuesto = presupuesto == null ? BigDecimal.ZERO : presupuesto;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
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
        if (!(object instanceof Departamento departamento)) {
            return false;
        }
        return id != null && id.equals(departamento.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}
