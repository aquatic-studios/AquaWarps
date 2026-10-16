package com.aquaticstudios.aquawarps.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Cooldowns {

    private final Map<UUID, Map<String, Long>> data = new ConcurrentHashMap<>();

    public long remaining(UUID player, String key) {
        Map<String, Long> entries = data.get(player);
        if (entries == null) {
            return 0L;
        }
        Long until = entries.get(key);
        if (until == null) {
            return 0L;
        }
        long left = until - System.currentTimeMillis();
        if (left <= 0L) {
            entries.remove(key);
            return 0L;
        }
        return left;
    }

    public boolean active(UUID player, String key) {
        return remaining(player, key) > 0L;
    }

    public void set(UUID player, String key, long millis) {
        if (millis <= 0L) {
            return;
        }
        data.computeIfAbsent(player, ignored -> new ConcurrentHashMap<>())
                .put(key, System.currentTimeMillis() + millis);
    }

    public void seconds(UUID player, String key, long seconds) {
        set(player, key, seconds * 1000L);
    }

    public void clear(UUID player, String key) {
        Map<String, Long> entries = data.get(player);
        if (entries != null) {
            entries.remove(key);
        }
    }

    public void clear(UUID player) {
        data.remove(player);
    }
}
