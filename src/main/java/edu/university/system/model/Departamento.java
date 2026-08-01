package edu.university.system.model;

/**
 * Entidad territorial asociada a un pais. Se usa para clasificar empresas y
 * empleados manteniendo la relacion bidireccional con Pais.
 */
public class Departamento {

    private Long id;
    private String nombre;
    private String codigo;
    private Pais pais;

    public Departamento() {
    }

    public Departamento(Long id, String nombre, String codigo, Pais pais) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        setPais(pais);
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        if (this.pais == pais) {
            return;
        }
        Pais paisAnterior = this.pais;
        this.pais = pais;
        if (paisAnterior != null) {
            paisAnterior.removerDepartamento(this);
        }
        if (pais != null && !pais.getDepartamentos().contains(this)) {
            pais.agregarDepartamento(this);
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
