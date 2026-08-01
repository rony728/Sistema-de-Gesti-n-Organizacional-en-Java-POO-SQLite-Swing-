package edu.university.system.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Proyecto gestionado para una empresa. Contiene fechas, presupuesto y
 * asignaciones de empleados.
 */
public class Proyecto {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal presupuesto;
    private Empresa empresa;
    private final List<Asignacion> asignaciones;

    public Proyecto() {
        this.asignaciones = new ArrayList<>();
    }

    public Proyecto(
            Long id,
            String codigo,
            String nombre,
            String descripcion,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            BigDecimal presupuesto,
            Empresa empresa
    ) {
        this();
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.presupuesto = presupuesto;
        setEmpresa(empresa);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(BigDecimal presupuesto) {
        this.presupuesto = presupuesto;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        if (this.empresa == empresa) {
            return;
        }
        Empresa empresaAnterior = this.empresa;
        this.empresa = empresa;
        if (empresaAnterior != null) {
            empresaAnterior.removerProyecto(this);
        }
        if (empresa != null && !empresa.getProyectos().contains(this)) {
            empresa.agregarProyecto(this);
        }
    }

    public List<Asignacion> getAsignaciones() {
        return Collections.unmodifiableList(asignaciones);
    }

    public void agregarAsignacion(Asignacion asignacion) {
        Objects.requireNonNull(asignacion, "La asignacion no puede ser nula.");
        if (!asignaciones.contains(asignacion)) {
            asignaciones.add(asignacion);
        }
        if (asignacion.getProyecto() != this) {
            asignacion.setProyecto(this);
        }
    }

    public void removerAsignacion(Asignacion asignacion) {
        if (asignacion == null) {
            return;
        }
        asignaciones.remove(asignacion);
        if (asignacion.getProyecto() == this) {
            asignacion.setProyecto(null);
        }
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
        if (!(object instanceof Proyecto proyecto)) {
            return false;
        }
        return id != null && id.equals(proyecto.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}
