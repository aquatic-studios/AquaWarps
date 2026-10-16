package com.aquaticstudios.aquawarps.database.type;

import com.aquaticstudios.aquawarps.AquaWarps;
import com.aquaticstudios.aquawarps.database.SQLStorage;
import com.aquaticstudios.aquawarps.util.Settings;

public final class MySQL extends SQLStorage {

    public MySQL(AquaWarps plugin) {
        super(plugin, "AquaWarps-MySQL");
    }

    @Override
    protected String driverClass() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    protected String jdbcUrl() {
        Settings settings = plugin.settings();
        return "jdbc:mysql://" + settings.mysqlHost() + ":" + settings.mysqlPort()
                + "/" + settings.mysqlDatabase()
                + "?useSSL=" + settings.mysqlSsl()
                + "&autoReconnect=true&characterEncoding=utf8";
    }

    @Override
    protected String username() {
        return plugin.settings().mysqlUsername();
    }

    @Override
    protected String password() {
        return plugin.settings().mysqlPassword();
    }

    @Override
    protected String upsert(String table, String secondColumn) {
        return "INSERT INTO " + table + " (id, " + secondColumn + ", data) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE " + secondColumn + " = VALUES(" + secondColumn + "), "
                + "data = VALUES(data)";
    }
}
