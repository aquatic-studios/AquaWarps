package com.aquaticstudios.aquawarps.library;

import com.aquaticstudios.aquawarps.database.DatabaseType;
import revxrsal.zapper.Dependency;

public enum Libraries {

    SQLITE(
            DatabaseType.SQLITE,
            new Dependency("org.xerial", "sqlite-jdbc", "3.42.0.0")
    ),

    MYSQL(
            DatabaseType.MYSQL,
            new Dependency("com.mysql", "mysql-connector-j", "9.2.0")
    ),

    MARIADB(
            DatabaseType.MARIADB,
            new Dependency("org.mariadb.jdbc", "mariadb-java-client", "3.4.1")
    ),

    H2(
            DatabaseType.H2,
            new Dependency("com.h2database", "h2", "2.1.214")
    );

    private final DatabaseType type;
    private final Dependency dependency;

    Libraries(DatabaseType type, Dependency dependency) {
        this.type = type;
        this.dependency = dependency;
    }

    public boolean isNeededFor(DatabaseType selected) {
        return type == selected;
    }

    public Dependency dependency() {
        return dependency;
    }
}
