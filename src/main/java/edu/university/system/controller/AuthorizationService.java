package edu.university.system.controller;

import edu.university.system.model.UserSession;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Servicio centralizado de autorizacion basado en la sesion activa. Evita que
 * los formularios dependan de comparaciones directas de nombres de rol y
 * permite reforzar permisos tambien desde los controladores.
 */
public final class AuthorizationService {

    private static final String ADMIN_ROLE = "ADMINISTRADOR";
    private static final String CONSULTA_ROLE = "CONSULTA";

    private AuthorizationService() {
    }

    public static boolean can(Permission permission) {
        UserSession session = UserSession.current();
        if (session == null || session.getRol() == null || session.getRol().getNombre() == null) {
            return false;
        }

        Set<Permission> permissions = permissionsFor(session.getRol().getNombre());
        return permissions.contains(permission);
    }

    public static void require(Permission permission) {
        if (!can(permission)) {
            throw new ValidationException("No tiene permisos para realizar esta accion.");
        }
    }

    private static Set<Permission> permissionsFor(String roleName) {
        String normalizedRole = normalize(roleName);
        if (ADMIN_ROLE.equals(normalizedRole)) {
            return EnumSet.allOf(Permission.class);
        }
        if (CONSULTA_ROLE.equals(normalizedRole)) {
            return EnumSet.of(Permission.READ, Permission.EXPORT, Permission.CHANGE_OWN_PASSWORD);
        }
        return EnumSet.noneOf(Permission.class);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
