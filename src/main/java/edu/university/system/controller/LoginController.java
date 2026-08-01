package edu.university.system.controller;

import edu.university.system.dao.UsuarioDao;
import edu.university.system.model.UserSession;
import edu.university.system.model.Usuario;
import edu.university.system.utils.PasswordUtils;

import java.util.Locale;
import java.util.Optional;

/**
 * Controlador de autenticacion. Normaliza el usuario, consulta SQLite mediante
 * UsuarioDao, valida el hash de contrasena y crea una sesion minima cuando las
 * credenciales son correctas.
 */
public final class LoginController {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Usuario o contrasena incorrectos.";

    private final UsuarioDao usuarioDao;

    public LoginController(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    public AuthenticationResult authenticate(String username, char[] password) {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername.isBlank() || password == null || password.length == 0) {
            PasswordUtils.clear(password);
            return AuthenticationResult.failed(INVALID_CREDENTIALS_MESSAGE);
        }

        try {
            Optional<Usuario> optionalUsuario = usuarioDao.buscarPorNombreUsuario(normalizedUsername);
            if (optionalUsuario.isEmpty()) {
                return AuthenticationResult.failed(INVALID_CREDENTIALS_MESSAGE);
            }

            Usuario usuario = optionalUsuario.get();
            if (!usuario.isActivo() || !PasswordUtils.SHA_256.equalsIgnoreCase(usuario.getPasswordAlgoritmo())) {
                return AuthenticationResult.failed(INVALID_CREDENTIALS_MESSAGE);
            }
            if (!PasswordUtils.matches(password, usuario.getPasswordHash())) {
                return AuthenticationResult.failed(INVALID_CREDENTIALS_MESSAGE);
            }

            usuarioDao.actualizarUltimoAcceso(usuario.getId());
            return AuthenticationResult.success(UserSession.start(usuario));
        } finally {
            PasswordUtils.clear(password);
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
