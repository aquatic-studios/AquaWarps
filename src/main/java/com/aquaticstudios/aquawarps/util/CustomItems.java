package com.aquaticstudios.aquawarps.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public final class CustomItems {

    public static ItemStack resolve(String raw) {
        if (raw == null) {
            return null;
        }
        int split = raw.indexOf(':');
        if (split <= 0) {
            return null;
        }
        String provider = raw.substring(0, split).trim().toLowerCase(Locale.ROOT);
        String id = raw.substring(split + 1).trim();
        if (id.isEmpty()) {
            return null;
        }
        switch (provider) {
            case "itemsadder":
            case "ia":
                return build("ItemsAdder", "dev.lone.itemsadder.api.CustomStack", "getInstance", id, "getItemStack");
            case "oraxen":
                return build("Oraxen", "io.th0rgal.oraxen.api.OraxenItems", "getItemById", id, "build");
            case "nexo":
                return build("Nexo", "com.nexomc.nexo.api.NexoItems", "itemFromId", id, "build");
            default:
                return null;
        }
    }

    private static ItemStack build(String plugin, String owner, String lookup, String id, String builder) {
        if (!Bukkit.getPluginManager().isPluginEnabled(plugin)) {
            return null;
        }
        try {
            Object handle = Class.forName(owner).getMethod(lookup, String.class).invoke(null, id);
            if (handle == null) {
                return null;
            }
            Object built = handle.getClass().getMethod(builder).invoke(handle);
            return built instanceof ItemStack ? ((ItemStack) built).clone() : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ex) {
            return null;
        }
    }
}
