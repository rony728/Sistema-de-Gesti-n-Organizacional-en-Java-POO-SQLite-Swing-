package edu.university.system.model;

import java.time.LocalDate;

/**
 * Base abstracta para datos personales compartidos. Empleado hereda esta clase
 * para separar identidad civil de datos laborales.
 */
public abstract class Persona {

    private Long id;
    private String identidad;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correoElectronico;
    private String direccion;
    private LocalDate fechaNacimiento;

    protected Persona() {
    }

    protected Persona(
            Long id,
            String identidad,
            String nombres,
            String apellidos,
            String telefono,
            String correoElectronico,
            String direccion,
            LocalDate fechaNacimiento
    ) {
        this.id = id;
        this.identidad = identidad;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentidad() {
        return identidad;
    }

    public void setIdentidad(String identidad) {
        this.identidad = identidad;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNombreCompleto() {
        String nombre = nombres == null ? "" : nombres.trim();
        String apellido = apellidos == null ? "" : apellidos.trim();
        return (nombre + " " + apellido).trim();
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Persona persona = (Persona) object;
        return id != null && id.equals(persona.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}
