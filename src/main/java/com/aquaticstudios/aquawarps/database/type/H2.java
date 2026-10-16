package com.aquaticstudios.aquawarps.database.type;

import com.aquaticstudios.aquawarps.AquaWarps;
import com.aquaticstudios.aquawarps.database.SQLStorage;

import java.io.File;

public final class H2 extends SQLStorage {

    public H2(AquaWarps plugin) {
        super(plugin, "AquaWarps-H2");
    }

    @Override
    protected String driverClass() {
        return "org.h2.Driver";
    }

    @Override
    protected String jdbcUrl() {
        File file = new File(plugin.getDataFolder(), "warps");
        return "jdbc:h2:" + file.getAbsolutePath() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE";
    }

    @Override
    protected String username() {
        return "sa";
    }

    @Override
    protected String password() {
        return "";
    }

    @Override
    protected String upsert(String table, String secondColumn) {
        return "MERGE INTO " + table + " (id, " + secondColumn + ", data) KEY(id) VALUES (?, ?, ?)";
    }
}
