package edu.university.system.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Empresa cliente o responsable de proyectos. Se vincula a pais, departamento y
 * mantiene una coleccion de proyectos relacionados.
 */
public class Empresa {

    private Long id;
    private String nombre;
    private String rtn;
    private String telefono;
    private String correoElectronico;
    private String direccion;
    private Pais pais;
    private Departamento departamento;
    private final List<Proyecto> proyectos;

    public Empresa() {
        this.proyectos = new ArrayList<>();
    }

    public Empresa(
            Long id,
            String nombre,
            String rtn,
            String telefono,
            String correoElectronico,
            String direccion,
            Pais pais,
            Departamento departamento
    ) {
        this();
        this.id = id;
        this.nombre = nombre;
        this.rtn = rtn;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.direccion = direccion;
        this.pais = pais;
        this.departamento = departamento;
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

    public String getRtn() {
        return rtn;
    }

    public void setRtn(String rtn) {
        this.rtn = rtn;
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

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public List<Proyecto> getProyectos() {
        return Collections.unmodifiableList(proyectos);
    }

    public void agregarProyecto(Proyecto proyecto) {
        Objects.requireNonNull(proyecto, "El proyecto no puede ser nulo.");
        if (!proyectos.contains(proyecto)) {
            proyectos.add(proyecto);
        }
        if (proyecto.getEmpresa() != this) {
            proyecto.setEmpresa(this);
        }
    }

    public void removerProyecto(Proyecto proyecto) {
        if (proyecto == null) {
            return;
        }
        proyectos.remove(proyecto);
        if (proyecto.getEmpresa() == this) {
            proyecto.setEmpresa(null);
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
        if (!(object instanceof Empresa empresa)) {
            return false;
        }
        return id != null && id.equals(empresa.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}
