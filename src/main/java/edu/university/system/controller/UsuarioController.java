package edu.university.system.controller;

import edu.university.system.dao.RolDao;
import edu.university.system.dao.UsuarioDao;
import edu.university.system.model.Rol;
import edu.university.system.model.UserSession;
import edu.university.system.model.Usuario;
import edu.university.system.utils.PasswordUtils;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de administracion de usuarios y cambio de contrasena. Aplica
 * reglas de seguridad como no desactivar el propio usuario ni dejar el sistema
 * sin administradores activos.
 */
public class UsuarioController extends ControllerSupport {

    private final UsuarioDao usuarioDao;
    private final RolDao rolDao;

    public UsuarioController(UsuarioDao usuarioDao, RolDao rolDao) {
        this.usuarioDao = usuarioDao;
        this.rolDao = rolDao;
    }

    public Long insertar(Usuario usuario, char[] password, char[] confirmation) {
        AuthorizationService.require(Permission.MANAGE_USERS);
        validarUsuarioNuevo(usuario);
        validarPasswordNueva(password, confirmation);
        if (usuarioDao.existeNombreUsuario(usuario.getNombreUsuario(), null)) {
            throw new ValidationException("El nombre de usuario ya existe.");
        }
        try {
            usuario.setPasswordHash(PasswordUtils.hashSha256(password));
            usuario.setPasswordAlgoritmo(PasswordUtils.SHA_256);
            usuario.setActivo(true);
            usuario.setDebeCambiarPassword(true);
            return usuarioDao.insertar(usuario);
        } finally {
            PasswordUtils.clear(password);
            PasswordUtils.clear(confirmation);
        }
    }

    public boolean actualizarDatosGenerales(Usuario usuario) {
        AuthorizationService.require(Permission.MANAGE_USERS);
        validarUsuarioExistente(usuario);
        requireId(usuario.getId(), "Usuario");
        Usuario currentUsuario = usuarioDao.buscarPorId(usuario.getId())
                .orElseThrow(() -> new ValidationException("Usuario no encontrado."));
        if (currentUsuario.isActivo()
                && esAdministrador(currentUsuario)
                && !esAdministrador(usuario)
                && usuarioDao.contarAdministradoresActivos() <= 1) {
            throw new ValidationException("No se puede quitar el rol al ultimo administrador activo.");
        }
        return usuarioDao.actualizarDatosGenerales(usuario);
    }

    public boolean activarDesactivar(Long id, boolean activo) {
        AuthorizationService.require(Permission.MANAGE_USERS);
        requireId(id, "Usuario");
        UserSession session = UserSession.current();
        if (session != null && id.equals(session.getUserId()) && !activo) {
            throw new ValidationException("No puede desactivar su propio usuario.");
        }
        Usuario usuario = usuarioDao.buscarPorId(id)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado."));
        if (!activo && esAdministrador(usuario) && usuarioDao.contarAdministradoresActivos() <= 1) {
            throw new ValidationException("No se puede desactivar el ultimo administrador activo.");
        }
        return usuarioDao.actualizarActivo(id, activo);
    }

    public boolean restablecerPassword(Long id, char[] password, char[] confirmation, boolean forceChange) {
        AuthorizationService.require(Permission.MANAGE_USERS);
        requireId(id, "Usuario");
        validarPasswordNueva(password, confirmation);
        try {
            return usuarioDao.actualizarPassword(id, PasswordUtils.hashSha256(password), PasswordUtils.SHA_256, forceChange);
        } finally {
            PasswordUtils.clear(password);
            PasswordUtils.clear(confirmation);
        }
    }

    public boolean cambiarPasswordPropia(char[] currentPassword, char[] newPassword, char[] confirmation) {
        AuthorizationService.require(Permission.CHANGE_OWN_PASSWORD);
        UserSession session = UserSession.current();
        if (session == null) {
            throw new ValidationException("No hay una sesion activa.");
        }
        Usuario usuario = usuarioDao.buscarPorId(session.getUserId())
                .orElseThrow(() -> new ValidationException("Usuario no encontrado."));
        try {
            if (!PasswordUtils.matches(currentPassword, usuario.getPasswordHash())) {
                throw new ValidationException("La contrasena actual no es correcta.");
            }
            if (PasswordUtils.matches(newPassword, usuario.getPasswordHash())) {
                throw new ValidationException("La nueva contrasena debe ser diferente de la actual.");
            }
            validarPasswordNueva(newPassword, confirmation);
            boolean updated = usuarioDao.actualizarPassword(
                    usuario.getId(),
                    PasswordUtils.hashSha256(newPassword),
                    PasswordUtils.SHA_256,
                    false
            );
            session.setDebeCambiarPassword(false);
            return updated;
        } finally {
            PasswordUtils.clear(currentPassword);
            PasswordUtils.clear(newPassword);
            PasswordUtils.clear(confirmation);
        }
    }

    public Optional<Usuario> buscarPorId(Long id) {
        AuthorizationService.require(Permission.MANAGE_USERS);
        requireId(id, "Usuario");
        return usuarioDao.buscarPorId(id);
    }

    public List<Usuario> listar() {
        AuthorizationService.require(Permission.MANAGE_USERS);
        return usuarioDao.listar();
    }

    public List<Usuario> buscar(String criterio) {
        AuthorizationService.require(Permission.MANAGE_USERS);
        if (criterio == null || criterio.isBlank()) {
            return listar();
        }
        return usuarioDao.buscar(criterio);
    }

    public List<Rol> listarRolesActivos() {
        AuthorizationService.require(Permission.MANAGE_USERS);
        return rolDao.listarActivos();
    }

    private void validarUsuarioNuevo(Usuario usuario) {
        validarUsuarioExistente(usuario);
        requireText(usuario.getNombreUsuario(), "Nombre de usuario", 3);
    }

    private void validarUsuarioExistente(Usuario usuario) {
        requireObject(usuario, "Usuario");
        requireObject(usuario.getRol(), "Rol del usuario");
        requireId(usuario.getRol().getId(), "Rol del usuario");
        requireText(usuario.getNombreMostrar(), "Nombre para mostrar", 2);
        validateOptionalEmail(usuario.getCorreoElectronico(), "Correo electronico");
    }

    private void validarPasswordNueva(char[] password, char[] confirmation) {
        if (password == null || confirmation == null) {
            throw new ValidationException("Ingrese y confirme la contrasena.");
        }
        if (password.length < 8) {
            throw new ValidationException("La contrasena debe tener al menos 8 caracteres.");
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char character : password) {
            hasUpper |= Character.isUpperCase(character);
            hasLower |= Character.isLowerCase(character);
            hasDigit |= Character.isDigit(character);
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new ValidationException("La contrasena debe incluir mayuscula, minuscula y numero.");
        }
        if (password.length != confirmation.length) {
            throw new ValidationException("La confirmacion no coincide.");
        }
        for (int index = 0; index < password.length; index++) {
            if (password[index] != confirmation[index]) {
                throw new ValidationException("La confirmacion no coincide.");
            }
        }
    }

    private boolean esAdministrador(Usuario usuario) {
        return usuario.getRol() != null
                && usuario.getRol().getNombre() != null
                && "administrador".equalsIgnoreCase(usuario.getRol().getNombre().trim());
    }
}
