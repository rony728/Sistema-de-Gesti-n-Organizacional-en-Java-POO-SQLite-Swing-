package edu.university.system.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad laboral basada en Persona. Relaciona cargo, departamento, salario,
 * fecha de contratacion, fotografia y asignaciones de proyectos.
 */
public class Empleado extends Persona {

    private String codigoEmpleado;
    private LocalDate fechaContratacion;
    private BigDecimal salario;
    private Cargo cargo;
    private Departamento departamento;
    private Pais pais;
    private String rutaFotografia;
    private final List<Asignacion> asignaciones;

    public Empleado() {
        this.asignaciones = new ArrayList<>();
    }

    public Empleado(
            Long id,
            String identidad,
            String nombres,
            String apellidos,
            String telefono,
            String correoElectronico,
            String direccion,
            LocalDate fechaNacimiento,
            String codigoEmpleado,
            LocalDate fechaContratacion,
            BigDecimal salario,
            Cargo cargo,
            Departamento departamento
    ) {
        this(id, identidad, nombres, apellidos, telefono, correoElectronico, direccion, fechaNacimiento,
                codigoEmpleado, fechaContratacion, salario, cargo, departamento, null, null);
    }

    public Empleado(
            Long id,
            String identidad,
            String nombres,
            String apellidos,
            String telefono,
            String correoElectronico,
            String direccion,
            LocalDate fechaNacimiento,
            String codigoEmpleado,
            LocalDate fechaContratacion,
            BigDecimal salario,
            Cargo cargo,
            Departamento departamento,
            String rutaFotografia
    ) {
        this(id, identidad, nombres, apellidos, telefono, correoElectronico, direccion, fechaNacimiento,
                codigoEmpleado, fechaContratacion, salario, cargo, departamento, rutaFotografia, null);
    }

    public Empleado(
            Long id,
            String identidad,
            String nombres,
            String apellidos,
            String telefono,
            String correoElectronico,
            String direccion,
            LocalDate fechaNacimiento,
            String codigoEmpleado,
            LocalDate fechaContratacion,
            BigDecimal salario,
            Cargo cargo,
            Departamento departamento,
            Pais pais
    ) {
        this(id, identidad, nombres, apellidos, telefono, correoElectronico, direccion, fechaNacimiento,
                codigoEmpleado, fechaContratacion, salario, cargo, departamento, null, pais);
    }

    public Empleado(
            Long id,
            String identidad,
            String nombres,
            String apellidos,
            String telefono,
            String correoElectronico,
            String direccion,
            LocalDate fechaNacimiento,
            String codigoEmpleado,
            LocalDate fechaContratacion,
            BigDecimal salario,
            Cargo cargo,
            Departamento departamento,
            String rutaFotografia,
            Pais pais
    ) {
        super(id, identidad, nombres, apellidos, telefono, correoElectronico, direccion, fechaNacimiento);
        this.asignaciones = new ArrayList<>();
        this.codigoEmpleado = codigoEmpleado;
        this.fechaContratacion = fechaContratacion;
        this.salario = salario;
        this.cargo = cargo;
        this.departamento = departamento;
        this.rutaFotografia = rutaFotografia;
        this.pais = pais;
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }

    public String getRutaFotografia() {
        return rutaFotografia;
    }

    public void setRutaFotografia(String rutaFotografia) {
        this.rutaFotografia = rutaFotografia;
    }

    public List<Asignacion> getAsignaciones() {
        return Collections.unmodifiableList(asignaciones);
    }

    public void agregarAsignacion(Asignacion asignacion) {
        Objects.requireNonNull(asignacion, "La asignacion no puede ser nula.");
        if (!asignaciones.contains(asignacion)) {
            asignaciones.add(asignacion);
        }
        if (asignacion.getEmpleado() != this) {
            asignacion.setEmpleado(this);
        }
    }

    public void removerAsignacion(Asignacion asignacion) {
        if (asignacion == null) {
            return;
        }
        asignaciones.remove(asignacion);
        if (asignacion.getEmpleado() == this) {
            asignacion.setEmpleado(null);
        }
    }

    @Override
    public String toString() {
        String nombreCompleto = getNombreCompleto();
        if (codigoEmpleado == null || codigoEmpleado.isBlank()) {
            return nombreCompleto;
        }
        return codigoEmpleado + " - " + nombreCompleto;
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
