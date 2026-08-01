package edu.university.system.model;

/**
 * Entidad de catalogo para paises. Se conserva como dato independiente de
 * ubicacion o nacionalidad y no es propietario de departamentos
 * organizacionales.
 */
public class Pais {

    private Long id;
    private String nombre;
    private String codigoIso;

    public Pais() {
    }

    public Pais(Long id, String nombre, String codigoIso) {
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
