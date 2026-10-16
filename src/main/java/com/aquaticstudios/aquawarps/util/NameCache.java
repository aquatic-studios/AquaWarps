package com.aquaticstudios.aquawarps.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NameCache {

    private final Map<UUID, String> names = new ConcurrentHashMap<>();
    private final Map<String, UUID> ids = new ConcurrentHashMap<>();

    public void warm() {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            String name = player.getName();
            if (name != null) {
                store(player.getUniqueId(), name);
            }
        }
    }

    public void store(UUID uuid, String name) {
        if (uuid == null || name == null || name.isEmpty()) {
            return;
        }
        names.put(uuid, name);
        ids.put(name.toLowerCase(Locale.ROOT), uuid);
    }

    public String get(UUID uuid) {
        if (uuid == null) {
            return "Unknown";
        }
        String cached = names.get(uuid);
        if (cached != null) {
            return cached;
        }
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            store(uuid, online.getName());
            return online.getName();
        }
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if (name != null) {
            store(uuid, name);
            return name;
        }
        return uuid.toString().substring(0, 8);
    }

    public UUID lookup(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        UUID cached = ids.get(name.toLowerCase(Locale.ROOT));
        if (cached != null) {
            return cached;
        }
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            store(online.getUniqueId(), online.getName());
            return online.getUniqueId();
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (offline.getName() != null && (offline.hasPlayedBefore() || offline.isOnline())) {
            store(offline.getUniqueId(), offline.getName());
            return offline.getUniqueId();
        }
        return null;
    }
}
