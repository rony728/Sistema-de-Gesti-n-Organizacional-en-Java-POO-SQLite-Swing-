package edu.university.system.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Empresa cliente o responsable de proyectos. Conserva su pais de ubicacion y
 * es propietaria de departamentos organizacionales sin cargarlos desde cada
 * consulta DAO.
 */
public class Empresa {

    private Long id;
    private String nombre;
    private String rtn;
    private String telefono;
    private String correoElectronico;
    private String direccion;
    private Pais pais;
    private final List<Departamento> departamentos;
    private final List<Proyecto> proyectos;

    public Empresa() {
        this.departamentos = new ArrayList<>();
        this.proyectos = new ArrayList<>();
    }

    public Empresa(
            Long id,
            String nombre,
            String rtn,
            String telefono,
            String correoElectronico,
            String direccion,
            Pais pais
    ) {
        this();
        this.id = id;
        this.nombre = nombre;
        this.rtn = rtn;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.direccion = direccion;
        this.pais = pais;
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

    public List<Departamento> getDepartamentos() {
        return Collections.unmodifiableList(departamentos);
    }

    public void agregarDepartamento(Departamento departamento) {
        Objects.requireNonNull(departamento, "El departamento no puede ser nulo.");
        if (!departamentos.contains(departamento)) {
            departamentos.add(departamento);
        }
        if (departamento.getEmpresa() != this) {
            departamento.setEmpresa(this);
        }
    }

    public void removerDepartamento(Departamento departamento) {
        if (departamento == null) {
            return;
        }
        departamentos.remove(departamento);
        if (departamento.getEmpresa() == this) {
            departamento.setEmpresa(null);
        }
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
