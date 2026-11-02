package com.quizsystem.model;

/** Roles a user account can have. */
public enum Role {
    ADMIN,
    PLAYER;

    /** Case-insensitive lookup that never throws. */
    public static Role from(String value) {
        if (value == null) {
            return PLAYER;
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        return PLAYER;
    }
}
