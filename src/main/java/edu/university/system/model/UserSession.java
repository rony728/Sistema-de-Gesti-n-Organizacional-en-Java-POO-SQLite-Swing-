package edu.university.system.model;

/**
 * Sesion minima del usuario autenticado. Mantiene un unico estado activo
 * durante la ejecucion y evita exponer publicamente objetos modificables de la
 * entidad Usuario completa.
 */
public final class UserSession {

    private static UserSession activeSession;

    private final Long userId;
    private final String username;
    private final String displayName;
    private final Rol rol;
    private boolean debeCambiarPassword;

    private UserSession(Long userId, String username, String displayName, Rol rol, boolean debeCambiarPassword) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.rol = rol;
        this.debeCambiarPassword = debeCambiarPassword;
    }

    public static synchronized UserSession start(Usuario usuario) {
        activeSession = new UserSession(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getNombreMostrar(),
                usuario.getRol(),
                usuario.isDebeCambiarPassword()
        );
        return activeSession;
    }

    public static synchronized UserSession current() {
        return activeSession;
    }

    public static synchronized void clear() {
        activeSession = null;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Rol getRol() {
        return rol;
    }

    public String getRoleName() {
        return rol == null ? "" : rol.getNombre();
    }

    public boolean isDebeCambiarPassword() {
        return debeCambiarPassword;
    }

    public void setDebeCambiarPassword(boolean debeCambiarPassword) {
        this.debeCambiarPassword = debeCambiarPassword;
    }
}
