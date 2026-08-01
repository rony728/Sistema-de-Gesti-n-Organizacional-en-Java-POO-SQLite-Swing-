package edu.university.system.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad de catalogo que agrupa departamentos y permite ubicar empresas y
 * empleados por pais.
 */
public class Pais {

    private Long id;
    private String nombre;
    private String codigoIso;
    private final List<Departamento> departamentos;

    public Pais() {
        this.departamentos = new ArrayList<>();
    }

    public Pais(Long id, String nombre, String codigoIso) {
        this();
        this.id = id;
        this.nombre = nombre;
        this.codigoIso = codigoIso;
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

    public String getCodigoIso() {
        return codigoIso;
    }

    public void setCodigoIso(String codigoIso) {
        this.codigoIso = codigoIso;
    }

    public List<Departamento> getDepartamentos() {
        return Collections.unmodifiableList(departamentos);
    }

    public void agregarDepartamento(Departamento departamento) {
        Objects.requireNonNull(departamento, "El departamento no puede ser nulo.");
        if (!departamentos.contains(departamento)) {
            departamentos.add(departamento);
        }
        if (departamento.getPais() != this) {
            departamento.setPais(this);
        }
    }

    public void removerDepartamento(Departamento departamento) {
        if (departamento == null) {
            return;
        }
        departamentos.remove(departamento);
        if (departamento.getPais() == this) {
            departamento.setPais(null);
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
        if (!(object instanceof Pais pais)) {
            return false;
        }
        return id != null && id.equals(pais.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : getClass().hashCode();
    }
}
