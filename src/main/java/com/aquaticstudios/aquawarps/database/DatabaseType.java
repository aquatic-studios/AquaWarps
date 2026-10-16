package com.aquaticstudios.aquawarps.database;

import java.util.Locale;

public enum DatabaseType {

    SQLITE("SQLite"),
    MYSQL("MySQL"),
    MARIADB("MariaDB"),
    H2("H2");

    private final String display;

    DatabaseType(String display) {
        this.display = display;
    }

    public String display() {
        return display;
    }

    public static DatabaseType fromString(String value) {
        if (value == null) {
            return SQLITE;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return SQLITE;
        }
    }
}
