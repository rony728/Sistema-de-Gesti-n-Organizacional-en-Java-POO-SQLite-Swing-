package edu.university.system.model;

import java.util.Objects;

/**
 * Representa un rol de seguridad del sistema. El rol define el conjunto de
 * permisos asignado por AuthorizationService durante la sesion activa.
 */
public class Rol {

    private Long id;
    private String nombre;
    private String descripcion;
    private boolean activo;

    public Rol() {
        this.activo = true;
    }

    public Rol(Long id, String nombre, String descripcion, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
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
        if (!(object instanceof Rol rol)) {
            return false;
        }
        return id != null && id.equals(rol.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : Objects.hash(getClass());
    }
}
