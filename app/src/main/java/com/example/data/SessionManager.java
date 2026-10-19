package com.example.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    // Domínio institucional definido em UM lugar só. Troque aqui e o app inteiro acompanha.
    public static final String EMAIL_DOMAIN = "[PREENCHER: domínio institucional]";

    private static final String PREF_NAME = "fmp_user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String name, String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "Guilherme Bettoni");
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "guilherme@" + EMAIL_DOMAIN);
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "Estudante da FMP • Matrícula [PREENCHER: matrícula]");
    }

    public boolean isSecretaria() {
        return "SECRETARIA".equalsIgnoreCase(getUserRole());
    }

    public boolean isGuest() {
        return "Comunidade / Visitante".equalsIgnoreCase(getUserRole())
                || (getUserEmail() != null && getUserEmail().startsWith("visitante@"));
    }

    public boolean isDarkMode() {
        return pref.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        editor.putBoolean(KEY_DARK_MODE, enabled);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
