package antifraud.api.auth.security;

import java.util.List;

public enum Role {
    ADMINISTRATOR, MERCHANT, SUPPORT;

    public static List<Role> getRoles() {
        return List.of(Role.values());
    }

    public static Role findRoleByAuthorityName(String authorityName) {
        return switch (authorityName) {
            case "ROLE_ADMINISTRATOR" -> Role.ADMINISTRATOR;
            case "ROLE_MERCHANT" -> Role.MERCHANT;
            case "ROLE_SUPPORT" -> Role.SUPPORT;
            default -> throw new IllegalArgumentException("Unknown authority name: " + authorityName);
        };
    }

    public static String getAuthorityNameByRole(Role role) {
        return "ROLE_" + role.name();
    }
}
