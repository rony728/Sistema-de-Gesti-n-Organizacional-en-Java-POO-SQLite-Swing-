package edu.university.system.controller;

import edu.university.system.model.UserSession;

public final class AuthenticationResult {

    private final boolean authenticated;
    private final UserSession session;
    private final String message;

    private AuthenticationResult(boolean authenticated, UserSession session, String message) {
        this.authenticated = authenticated;
        this.session = session;
        this.message = message;
    }

    public static AuthenticationResult success(UserSession session) {
        return new AuthenticationResult(true, session, "Autenticacion correcta.");
    }

    public static AuthenticationResult failed(String message) {
        return new AuthenticationResult(false, null, message);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public UserSession getSession() {
        return session;
    }

    public String getMessage() {
        return message;
    }
}
