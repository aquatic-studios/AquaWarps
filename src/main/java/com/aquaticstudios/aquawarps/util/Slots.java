package com.aquaticstudios.aquawarps.util;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public final class Slots {

    public static List<Integer> parse(ConfigurationSection section, String key) {
        List<Integer> slots = new ArrayList<>();
        Object value = section.get(key);
        if (value instanceof List) {
            for (Object entry : (List<?>) value) {
                add(slots, String.valueOf(entry));
            }
        } else if (value != null) {
            add(slots, String.valueOf(value));
        }
        return slots;
    }

    private static void add(List<Integer> slots, String raw) {
        for (String part : raw.split(",")) {
            String token = part.trim();
            int dash = token.indexOf('-', 1);
            try {
                if (dash == -1) {
                    slots.add(Integer.parseInt(token));
                    continue;
                }
                int from = Integer.parseInt(token.substring(0, dash).trim());
                int to = Integer.parseInt(token.substring(dash + 1).trim());
                for (int slot = Math.min(from, to); slot <= Math.max(from, to); slot++) {
                    slots.add(slot);
                }
            } catch (NumberFormatException ignored) {

            }
        }
    }
}
