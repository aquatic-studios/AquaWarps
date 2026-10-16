package com.aquaticstudios.aquawarps.database.type;

import com.aquaticstudios.aquawarps.AquaWarps;
import com.aquaticstudios.aquawarps.database.SQLStorage;

import java.io.File;

public final class SQLite extends SQLStorage {

    public SQLite(AquaWarps plugin) {
        super(plugin, "AquaWarps-SQLite");
    }

    @Override
    protected String driverClass() {
        return "org.sqlite.JDBC";
    }

    @Override
    protected String jdbcUrl() {
        File file = new File(plugin.getDataFolder(), "warps.db");
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }

    @Override
    protected String username() {
        return null;
    }

    @Override
    protected String password() {
        return null;
    }

    @Override
    protected String upsert(String table, String secondColumn) {
        return "INSERT INTO " + table + " (id, " + secondColumn + ", data) VALUES (?, ?, ?) "
                + "ON CONFLICT(id) DO UPDATE SET " + secondColumn + " = excluded." + secondColumn
                + ", data = excluded.data";
    }
}
