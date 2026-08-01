package edu.university.system.model;

import java.time.LocalDate;

/**
 * Relacion operativa entre empleado y proyecto. Define rol, rango de fechas y
 * horas asignadas.
 */
public class Asignacion {

    private Long id;
    private Empleado empleado;
    private Proyecto proyecto;
    private String rol;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int horasAsignadas;

    public Asignacion() {
    }

    public Asignacion(
            Long id,
            Empleado empleado,
            Proyecto proyecto,
            String rol,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            int horasAsignadas
    ) {
        this.id = id;
        this.rol = rol;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.horasAsignadas = horasAsignadas;
        setEmpleado(empleado);
        setProyecto(proyecto);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        if (this.empleado == empleado) {
            return;
        }
        Empleado empleadoAnterior = this.empleado;
        this.empleado = empleado;
        if (empleadoAnterior != null) {
            empleadoAnterior.removerAsignacion(this);
        }
        if (empleado != null && !empleado.getAsignaciones().contains(this)) {
            empleado.agregarAsignacion(this);
        }
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        if (this.proyecto == proyecto) {
            return;
        }
        Proyecto proyectoAnterior = this.proyecto;
        this.proyecto = proyecto;
        if (proyectoAnterior != null) {
            proyectoAnterior.removerAsignacion(this);
        }
        if (proyecto != null && !proyecto.getAsignaciones().contains(this)) {
            proyecto.agregarAsignacion(this);
        }
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
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

    public int getHorasAsignadas() {
        return horasAsignadas;
    }

    public void setHorasAsignadas(int horasAsignadas) {
        this.horasAsignadas = horasAsignadas;
    }

    @Override
    public String toString() {
        String nombreEmpleado = empleado == null ? "Sin empleado" : empleado.getNombreCompleto();
        String nombreProyecto = proyecto == null ? "Sin proyecto" : proyecto.getNombre();
        return nombreEmpleado + " - " + nombreProyecto;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Asignacion asignacion)) {
            return false;
        }
        return id != null && id.equals(asignacion.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}
