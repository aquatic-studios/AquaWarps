package com.aquaticstudios.aquawarps.database;

import com.aquaticstudios.aquawarps.AquaWarps;
import com.aquaticstudios.aquawarps.model.PlayerData;
import com.aquaticstudios.aquawarps.model.Rating;
import com.aquaticstudios.aquawarps.model.Report;
import com.aquaticstudios.aquawarps.model.Warp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SQLStorage implements StorageProvider {

    public static final String WARPS = "aquawarps_warps";
    public static final String PLAYERS = "aquawarps_players";
    public static final String RATINGS = "aquawarps_ratings";
    public static final String REPORTS = "aquawarps_reports";
    public static final String VISITS = "aquawarps_visits";

    protected final AquaWarps plugin;
    protected final ExecutorService executor;
    private Connection connection;

    protected SQLStorage(AquaWarps plugin, String threadName) {
        this.plugin = plugin;
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(true);
            return thread;
        });
    }

    protected abstract String driverClass();

    protected abstract String jdbcUrl();

    protected abstract String username();

    protected abstract String password();

    protected abstract String upsert(String table, String secondColumn);

    protected Connection connection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            try {
                Class.forName(driverClass());
            } catch (ClassNotFoundException ex) {
                throw new SQLException("JDBC driver not found: " + driverClass(), ex);
            }
            String user = username();
            connection = user == null
                    ? DriverManager.getConnection(jdbcUrl())
                    : DriverManager.getConnection(jdbcUrl(), user, password());
        }
        return connection;
    }

    @Override
    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            try (Statement statement = connection().createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + WARPS + " ("
                        + "id VARCHAR(36) NOT NULL PRIMARY KEY, "
                        + "name VARCHAR(64) NOT NULL, "
                        + "data TEXT NOT NULL)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + PLAYERS + " ("
                        + "id VARCHAR(36) NOT NULL PRIMARY KEY, "
                        + "name VARCHAR(64), "
                        + "data TEXT NOT NULL)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + RATINGS + " ("
                        + "id VARCHAR(80) NOT NULL PRIMARY KEY, "
                        + "warp VARCHAR(36) NOT NULL, "
                        + "data TEXT NOT NULL)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + REPORTS + " ("
                        + "id VARCHAR(36) NOT NULL PRIMARY KEY, "
                        + "warp VARCHAR(36) NOT NULL, "
                        + "data TEXT NOT NULL)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + VISITS + " ("
                        + "warp VARCHAR(36) NOT NULL, "
                        + "player VARCHAR(36) NOT NULL, "
                        + "time BIGINT NOT NULL)");
                index(statement, "CREATE INDEX idx_aquawarps_rating_warp ON " + RATINGS + " (warp)");
                index(statement, "CREATE INDEX idx_aquawarps_visit_time ON " + VISITS + " (time)");
                index(statement, "CREATE INDEX idx_aquawarps_visit_warp ON " + VISITS + " (warp)");
            } catch (SQLException ex) {
                throw new IllegalStateException("Failed to initialise AquaWarps tables", ex);
            }
        }, executor);
    }

    private void index(Statement statement, String sql) {
        try {
            statement.executeUpdate(sql);
        } catch (SQLException ignored) {
            return;
        }
    }

    @Override
    public CompletableFuture<List<Warp>> loadWarps() {
        return CompletableFuture.supplyAsync(() -> {
            List<Warp> warps = new ArrayList<>();
            try (PreparedStatement statement = connection().prepareStatement("SELECT id, data FROM " + WARPS);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Warp warp = Serializer.warp(result.getString("id"), result.getString("data"));
                    if (warp != null) {
                        warps.add(warp);
                    }
                }
            } catch (SQLException ex) {
                throw new IllegalStateException("Failed to load warps", ex);
            }
            return warps;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveWarp(Warp warp) {
        return CompletableFuture.runAsync(() -> writeWarp(warp), executor);
    }

    @Override
    public CompletableFuture<Void> saveWarps(Collection<Warp> warps) {
        List<Warp> snapshot = new ArrayList<>(warps);
        return CompletableFuture.runAsync(() -> {
            for (Warp warp : snapshot) {
                writeWarp(warp);
            }
        }, executor);
    }

    private void writeWarp(Warp warp) {
        try (PreparedStatement statement = connection().prepareStatement(upsert(WARPS, "name"))) {
            statement.setString(1, warp.id());
            statement.setString(2, warp.name());
            statement.setString(3, Serializer.warp(warp));
            statement.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().warning("Failed to save warp " + warp.name() + ": " + ex.getMessage());
        }
    }

    @Override
    public CompletableFuture<Void> deleteWarp(Warp warp) {
        return CompletableFuture.runAsync(() -> {
            delete(WARPS, "id", warp.id());
            delete(RATINGS, "warp", warp.id());
            delete(REPORTS, "warp", warp.id());
            delete(VISITS, "warp", warp.id());
        }, executor);
    }

    private void delete(String table, String column, String value) {
        try (PreparedStatement statement = connection()
                .prepareStatement("DELETE FROM " + table + " WHERE " + column + " = ?")) {
            statement.setString(1, value);
            statement.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().warning("Failed to delete from " + table + ": " + ex.getMessage());
        }
    }

    @Override
    public CompletableFuture<PlayerData> loadPlayer(UUID uuid, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection()
                    .prepareStatement("SELECT data FROM " + PLAYERS + " WHERE id = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        PlayerData data = Serializer.player(uuid, result.getString("data"), name);
                        if (data != null) {
                            data.name(name);
                            return data;
                        }
                    }
                }
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to load player " + name + ": " + ex.getMessage());
            }
            PlayerData fresh = new PlayerData(uuid, name);
            fresh.pearls(plugin.settings().pearlsStarting());
            return fresh;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> savePlayer(PlayerData data) {
        String serialized = Serializer.player(data);
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement statement = connection().prepareStatement(upsert(PLAYERS, "name"))) {
                statement.setString(1, data.uuid().toString());
                statement.setString(2, data.name());
                statement.setString(3, serialized);
                statement.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to save player " + data.name() + ": " + ex.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Map<UUID, Long>> loadLastSeen() {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Long> out = new HashMap<>();
            try (PreparedStatement statement = connection().prepareStatement("SELECT id, data FROM " + PLAYERS);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    UUID uuid = parse(result.getString("id"));
                    if (uuid == null) {
                        continue;
                    }
                    PlayerData data = Serializer.player(uuid, result.getString("data"), "Unknown");
                    if (data != null) {
                        out.put(uuid, data.lastSeen());
                    }
                }
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to read last seen data: " + ex.getMessage());
            }
            return out;
        }, executor);
    }

    @Override
    public CompletableFuture<List<Rating>> loadRatings(String warpId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Rating> ratings = new ArrayList<>();
            try (PreparedStatement statement = connection()
                    .prepareStatement("SELECT data FROM " + RATINGS + " WHERE warp = ?")) {
                statement.setString(1, warpId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        Rating rating = Serializer.rating(result.getString("data"));
                        if (rating != null) {
                            ratings.add(rating);
                        }
                    }
                }
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to load reviews: " + ex.getMessage());
            }
            ratings.sort((left, right) -> Long.compare(right.created(), left.created()));
            return ratings;
        }, executor);
    }

    @Override
    public CompletableFuture<Map<String, Rating>> loadPlayerRatings(UUID player) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Rating> ratings = new HashMap<>();
            try (PreparedStatement statement = connection()
                    .prepareStatement("SELECT data FROM " + RATINGS + " WHERE id LIKE ?")) {
                statement.setString(1, "%:" + player);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        Rating rating = Serializer.rating(result.getString("data"));
                        if (rating != null && rating.player().equals(player)) {
                            ratings.put(rating.warpId(), rating);
                        }
                    }
                }
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to load player reviews: " + ex.getMessage());
            }
            return ratings;
        }, executor);
    }

    @Override
    public CompletableFuture<Map<String, Integer>> loadFavoriteCounts() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Integer> counts = new HashMap<>();
            try (PreparedStatement statement = connection().prepareStatement("SELECT id, data FROM " + PLAYERS);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    UUID uuid = parse(result.getString("id"));
                    if (uuid == null) {
                        continue;
                    }
                    PlayerData data = Serializer.player(uuid, result.getString("data"), "Unknown");
                    if (data == null) {
                        continue;
                    }
                    for (String warpId : data.favorites()) {
                        counts.merge(warpId, 1, Integer::sum);
                    }
                }
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to count favorites: " + ex.getMessage());
            }
            return counts;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveRating(Rating rating) {
        String serialized = Serializer.rating(rating);
        String id = rating.warpId() + ":" + rating.player();
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement statement = connection().prepareStatement(upsert(RATINGS, "warp"))) {
                statement.setString(1, id);
                statement.setString(2, rating.warpId());
                statement.setString(3, serialized);
                statement.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to save review: " + ex.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> deleteRating(String warpId, UUID player) {
        return CompletableFuture.runAsync(() -> delete(RATINGS, "id", warpId + ":" + player), executor);
    }

    @Override
    public CompletableFuture<List<Report>> loadReports() {
        return CompletableFuture.supplyAsync(() -> {
            List<Report> reports = new ArrayList<>();
            try (PreparedStatement statement = connection().prepareStatement("SELECT id, data FROM " + REPORTS);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Report report = Serializer.report(result.getString("id"), result.getString("data"));
                    if (report != null && !report.handled()) {
                        reports.add(report);
                    }
                }
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to load reports: " + ex.getMessage());
            }
            reports.sort((left, right) -> Long.compare(right.created(), left.created()));
            return reports;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveReport(Report report) {
        String serialized = Serializer.report(report);
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement statement = connection().prepareStatement(upsert(REPORTS, "warp"))) {
                statement.setString(1, report.id());
                statement.setString(2, report.warpId());
                statement.setString(3, serialized);
                statement.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to save report: " + ex.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<Visit>> loadVisits(long since) {
        return CompletableFuture.supplyAsync(() -> {
            List<Visit> visits = new ArrayList<>();
            try (PreparedStatement statement = connection()
                    .prepareStatement("SELECT warp, player, time FROM " + VISITS + " WHERE time >= ?")) {
                statement.setLong(1, since);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        UUID player = parse(result.getString("player"));
                        visits.add(new Visit(result.getString("warp"), player, result.getLong("time")));
                    }
                }
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to load visits: " + ex.getMessage());
            }
            return visits;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveVisit(String warpId, UUID player) {
        long now = System.currentTimeMillis();
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement statement = connection()
                    .prepareStatement("INSERT INTO " + VISITS + " (warp, player, time) VALUES (?, ?, ?)")) {
                statement.setString(1, warpId);
                statement.setString(2, player.toString());
                statement.setLong(3, now);
                statement.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to store visit: " + ex.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> purgeVisits(long before) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement statement = connection()
                    .prepareStatement("DELETE FROM " + VISITS + " WHERE time < ?")) {
                statement.setLong(1, before);
                statement.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().warning("Failed to purge visits: " + ex.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Long> count(String table) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection()
                    .prepareStatement("SELECT COUNT(*) AS total FROM " + table);
                 ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong("total") : 0L;
            } catch (SQLException ex) {
                return 0L;
            }
        }, executor);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            return;
        }
    }

    private static UUID parse(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
