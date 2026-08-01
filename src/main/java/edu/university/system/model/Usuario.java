package edu.university.system.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Usuario persistido para autenticacion y administracion. Guarda solo el hash
 * de contrasena y metadatos de seguridad; nunca conserva la contrasena en texto
 * plano.
 */
public class Usuario {

    private Long id;
    private Rol rol;
    private String nombreUsuario;
    private String passwordHash;
    private String passwordAlgoritmo;
    private String nombreMostrar;
    private String correoElectronico;
    private boolean activo;
    private boolean debeCambiarPassword;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public Usuario() {
        this.activo = true;
        this.debeCambiarPassword = true;
        this.passwordAlgoritmo = "SHA-256";
    }

    public Usuario(
            Long id,
            Rol rol,
            String nombreUsuario,
            String passwordHash,
            String passwordAlgoritmo,
            String nombreMostrar,
            String correoElectronico,
            boolean activo,
            boolean debeCambiarPassword,
            LocalDateTime ultimoAcceso,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion
    ) {
        this.id = id;
        this.rol = rol;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.passwordAlgoritmo = passwordAlgoritmo;
        this.nombreMostrar = nombreMostrar;
        this.correoElectronico = correoElectronico;
        this.activo = activo;
        this.debeCambiarPassword = debeCambiarPassword;
        this.ultimoAcceso = ultimoAcceso;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordAlgoritmo() {
        return passwordAlgoritmo;
    }

    public void setPasswordAlgoritmo(String passwordAlgoritmo) {
        this.passwordAlgoritmo = passwordAlgoritmo;
    }

    public String getNombreMostrar() {
        return nombreMostrar;
    }

    public void setNombreMostrar(String nombreMostrar) {
        this.nombreMostrar = nombreMostrar;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isDebeCambiarPassword() {
        return debeCambiarPassword;
    }

    public void setDebeCambiarPassword(boolean debeCambiarPassword) {
        this.debeCambiarPassword = debeCambiarPassword;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return nombreUsuario;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Usuario usuario)) {
            return false;
        }
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : Objects.hash(getClass());
    }
}
