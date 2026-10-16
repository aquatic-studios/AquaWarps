package com.aquaticstudios.aquawarps.placeholder;

import com.aquaticstudios.aquawarps.AquaWarps;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public final class WarpExpansion extends PlaceholderExpansion {

    private final AquaWarps plugin;

    public WarpExpansion(AquaWarps plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "aquawarps";
    }

    @Override
    public String getAuthor() {
        return "Senkex";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        return Placeholders.value(plugin, player, params);
    }
}
