package com.aquaticstudios.aquawarps.database;

import com.aquaticstudios.aquawarps.model.PlayerData;
import com.aquaticstudios.aquawarps.model.Rating;
import com.aquaticstudios.aquawarps.model.Report;
import com.aquaticstudios.aquawarps.model.Warp;
import com.aquaticstudios.aquawarps.model.WarpAccess;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Serializer {

    public static String warp(Warp warp) {
        YamlConfiguration data = new YamlConfiguration();
        data.set("owner", warp.owner().toString());
        data.set("owner-name", warp.ownerName());
        data.set("name", warp.name());
        data.set("description", warp.description());
        data.set("category", warp.category());
        data.set("world", warp.world());
        data.set("x", warp.x());
        data.set("y", warp.y());
        data.set("z", warp.z());
        data.set("yaw", warp.yaw());
        data.set("pitch", warp.pitch());
        data.set("icon", warp.icon());
        data.set("particle", warp.particle());
        data.set("access", warp.access().name());
        data.set("password", warp.password());
        data.set("fee", warp.fee());
        data.set("open", warp.open());
        data.set("hidden", warp.hidden());
        data.set("featured", warp.featured());
        data.set("visits", warp.visits());
        data.set("rating-sum", warp.ratingSum());
        data.set("rating-count", warp.ratingCount());
        data.set("experience", warp.experience());
        data.set("level", warp.level());
        data.set("tags", new ArrayList<>(warp.tags()));
        data.set("created", warp.created());
        data.set("updated", warp.updated());
        data.set("promoted-until", warp.promotedUntil());
        data.set("promotion-tier", warp.promotionTier());
        data.set("banned", uuids(warp.banned()));
        data.set("trusted", uuids(warp.trusted()));
        return data.saveToString();
    }

    public static Warp warp(String id, String raw) {
        YamlConfiguration data = read(raw);
        if (data == null) {
            return null;
        }
        UUID owner = uuid(data.getString("owner"));
        if (owner == null) {
            return null;
        }
        Warp warp = new Warp(id, owner, data.getString("owner-name", "Unknown"), data.getString("name", id));
        warp.description(data.getString("description", ""));
        warp.category(data.getString("category"));
        warp.rawLocation(data.getString("world"), data.getDouble("x"), data.getDouble("y"),
                data.getDouble("z"), (float) data.getDouble("yaw"), (float) data.getDouble("pitch"));
        warp.icon(data.getString("icon", "GRASS_BLOCK"));
        warp.particle(data.getString("particle", "NONE"));
        warp.access(WarpAccess.fromString(data.getString("access")));
        warp.password(data.getString("password"));
        warp.fee(data.getDouble("fee"));
        warp.open(data.getBoolean("open", true));
        warp.hidden(data.getBoolean("hidden", false));
        warp.featured(data.getBoolean("featured", false));
        warp.visits(data.getLong("visits"));
        warp.ratings(data.getLong("rating-sum"), data.getLong("rating-count"));
        warp.experience(data.getLong("experience"));
        warp.level(Math.max(1, data.getInt("level", 1)));
        warp.tags(data.getStringList("tags"));
        warp.created(data.getLong("created"));
        warp.updated(data.getLong("updated"));
        warp.promotedUntil(data.getLong("promoted-until"));
        warp.promotionTier(data.getString("promotion-tier"));
        for (String value : data.getStringList("banned")) {
            UUID parsed = uuid(value);
            if (parsed != null) {
                warp.banned().add(parsed);
            }
        }
        for (String value : data.getStringList("trusted")) {
            UUID parsed = uuid(value);
            if (parsed != null) {
                warp.trusted().add(parsed);
            }
        }
        warp.markClean();
        return warp;
    }

    public static String player(PlayerData player) {
        YamlConfiguration data = new YamlConfiguration();
        data.set("name", player.name());
        data.set("pearls", player.pearls());
        data.set("extra-slots", player.extraSlots());
        data.set("last-seen", player.lastSeen());
        data.set("favorites", new ArrayList<>(player.favorites()));
        data.set("stamps", new ArrayList<>(player.stamps()));
        data.set("recent", new ArrayList<>(player.recentStamps()));
        List<String> milestones = new ArrayList<>();
        for (Integer milestone : player.milestones()) {
            milestones.add(String.valueOf(milestone));
        }
        data.set("milestones", milestones);
        return data.saveToString();
    }

    public static PlayerData player(UUID uuid, String raw, String fallbackName) {
        YamlConfiguration data = read(raw);
        if (data == null) {
            return null;
        }
        PlayerData player = new PlayerData(uuid, data.getString("name", fallbackName));
        player.pearls(data.getDouble("pearls"));
        player.extraSlots(data.getInt("extra-slots"));
        player.lastSeen(data.getLong("last-seen"));
        player.favorites().addAll(data.getStringList("favorites"));
        player.stamps().addAll(data.getStringList("stamps"));
        player.recentStamps(data.getStringList("recent"));
        for (String value : data.getStringList("milestones")) {
            try {
                player.milestones().add(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
                continue;
            }
        }
        player.markClean();
        return player;
    }

    public static String rating(Rating rating) {
        YamlConfiguration data = new YamlConfiguration();
        data.set("warp", rating.warpId());
        data.set("player", rating.player().toString());
        data.set("player-name", rating.playerName());
        data.set("stars", rating.stars());
        data.set("comment", rating.comment());
        data.set("created", rating.created());
        return data.saveToString();
    }

    public static Rating rating(String raw) {
        YamlConfiguration data = read(raw);
        if (data == null) {
            return null;
        }
        UUID player = uuid(data.getString("player"));
        if (player == null) {
            return null;
        }
        return new Rating(data.getString("warp", ""), player, data.getString("player-name", "Unknown"),
                data.getInt("stars", 1), data.getString("comment", ""), data.getLong("created"));
    }

    public static String report(Report report) {
        YamlConfiguration data = new YamlConfiguration();
        data.set("warp", report.warpId());
        data.set("reporter", report.reporter().toString());
        data.set("reporter-name", report.reporterName());
        data.set("reason", report.reason());
        data.set("created", report.created());
        data.set("handled", report.handled());
        return data.saveToString();
    }

    public static Report report(String id, String raw) {
        YamlConfiguration data = read(raw);
        if (data == null) {
            return null;
        }
        UUID reporter = uuid(data.getString("reporter"));
        if (reporter == null) {
            return null;
        }
        return new Report(id, data.getString("warp", ""), reporter,
                data.getString("reporter-name", "Unknown"), data.getString("reason", "-"),
                data.getLong("created"), data.getBoolean("handled", false));
    }

    private static YamlConfiguration read(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        YamlConfiguration data = new YamlConfiguration();
        try {
            data.loadFromString(raw);
        } catch (InvalidConfigurationException ex) {
            return null;
        }
        return data;
    }

    private static List<String> uuids(Iterable<UUID> values) {
        List<String> out = new ArrayList<>();
        for (UUID value : values) {
            out.add(value.toString());
        }
        return out;
    }

    private static UUID uuid(String raw) {
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
