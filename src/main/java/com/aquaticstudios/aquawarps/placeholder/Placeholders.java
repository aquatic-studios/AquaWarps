package com.aquaticstudios.aquawarps.placeholder;

import com.aquaticstudios.aquawarps.AquaWarps;
import com.aquaticstudios.aquawarps.menu.Toggles;
import com.aquaticstudios.aquawarps.model.PlayerData;
import com.aquaticstudios.aquawarps.model.SortMode;
import com.aquaticstudios.aquawarps.model.Warp;
import com.aquaticstudios.aquawarps.util.CC;
import com.aquaticstudios.aquawarps.util.Formats;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Placeholders {

    private static final Pattern PATTERN = Pattern.compile("%aquawarps_([a-z0-9_]+)%");

    public static String apply(AquaWarps plugin, Player viewer, String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String result = text;
        if (result.indexOf('%') >= 0) {
            Matcher matcher = PATTERN.matcher(result);
            StringBuffer builder = new StringBuffer();
            while (matcher.find()) {
                String value = value(plugin, viewer, matcher.group(1));
                matcher.appendReplacement(builder,
                        Matcher.quoteReplacement(value == null ? matcher.group() : value));
            }
            matcher.appendTail(builder);
            result = builder.toString();
        }
        if (viewer != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(viewer, result);
        }
        return result;
    }

    public static String value(AquaWarps plugin, OfflinePlayer player, String identifier) {
        String key = identifier.toLowerCase(Locale.ROOT);

        if (key.startsWith("top_")) {
            return top(plugin, key);
        }

        switch (key) {
            case "total":
                return String.valueOf(plugin.warps().total());
            case "promoted":
                return String.valueOf(plugin.warps().activePromotions());
            case "hidden":
                return String.valueOf(plugin.warps().hiddenCount());
            case "categories":
                return String.valueOf(plugin.categories().all().size());
            case "reports":
                return String.valueOf(plugin.warps().reports().size());
            case "currency":
                return plugin.settings().pearlsName();
            case "create_cost":
                return plugin.economy().formatDefault(plugin.settings().createCost());
            default:
                break;
        }

        if (player == null) {
            return "";
        }
        PlayerData data = plugin.profiles().get(player.getUniqueId());

        switch (key) {
            case "warps":
                return String.valueOf(plugin.warps().countOf(player.getUniqueId()));
            case "limit":
                return player.isOnline() && player.getPlayer() != null
                        ? limit(plugin, player.getPlayer())
                        : String.valueOf(plugin.settings().limitDefaultValue());
            case "pearls":
                return data == null ? "0" : Formats.money(data.pearls());
            case "pearls_raw":
                return data == null ? "0" : String.valueOf((long) data.pearls());
            case "extra_slots":
                return data == null ? "0" : String.valueOf(data.extraSlots());
            case "max_extra_slots":
                return String.valueOf(plugin.settings().extraSlotMax());
            case "slot_cost":
                return data == null ? "-" : plugin.economy().formatDefault(slotCost(plugin, data));
            case "stamps":
                return data == null ? "0" : String.valueOf(data.stampCount());
            case "favorites":
                return data == null ? "0" : String.valueOf(data.favorites().size());
            case "rank":
                return data == null ? "" : plugin.passport().rank(data.stampCount());
            case "rank_plain":
                return data == null ? "" : plugin.passport().rankPlain(data.stampCount());
            case "rank_progress":
                return data == null ? "" : plugin.passport().progressBar(data.stampCount());
            case "next_rank":
                return nextRank(plugin, data);
            case "visits":
                return Formats.number(plugin.warps().visitsOf(player.getUniqueId()));
            case "sort":
                return data == null
                        ? ""
                        : plugin.messages().format("sort." + data.sort().name().toLowerCase(Locale.ROOT));
            case "sort_list":
                return data == null ? "" : sortList(plugin, data);
            case "filter":
                return data == null || data.categoryFilter() == null
                        ? plugin.messages().format("state.all")
                        : plugin.categories().display(data.categoryFilter());
            case "search":
                return data == null || data.searchQuery() == null || data.searchQuery().isEmpty()
                        ? plugin.messages().format("state.none")
                        : data.searchQuery();
            default:
                return null;
        }
    }

    private static String sortList(AquaWarps plugin, PlayerData data) {
        List<String> options = new ArrayList<>();
        for (SortMode mode : SortMode.values()) {
            options.add(plugin.messages().format("sort." + mode.name().toLowerCase(Locale.ROOT)));
        }
        return Toggles.list(plugin, options, data.sort().ordinal());
    }

    private static String limit(AquaWarps plugin, Player player) {
        int limit = plugin.warps().limit(player);
        return limit == Integer.MAX_VALUE
                ? plugin.messages().format("state.unlimited")
                : String.valueOf(limit);
    }

    private static double slotCost(AquaWarps plugin, PlayerData data) {
        return plugin.settings().extraSlotCost()
                * Math.pow(plugin.settings().extraSlotMultiplier(), data.extraSlots());
    }

    private static String nextRank(AquaWarps plugin, PlayerData data) {
        if (data == null) {
            return "";
        }
        java.util.Map.Entry<Integer, String> next = plugin.passport().nextRank(data.stampCount());
        return next == null ? plugin.messages().format("state.max-rank") : next.getValue();
    }

    private static String top(AquaWarps plugin, String key) {
        String[] parts = key.split("_");
        if (parts.length < 4) {
            return "";
        }
        SortMode mode = SortMode.fromString(parts[1], SortMode.VISITS);
        int position;
        try {
            position = Integer.parseInt(parts[2]);
        } catch (NumberFormatException ex) {
            return "";
        }
        String field = parts[3];

        List<Warp> warps = plugin.warps().visible(null, false);
        plugin.warps().sort(warps, mode);
        if (position < 1 || position > warps.size()) {
            return "-";
        }
        Warp warp = warps.get(position - 1);

        switch (field) {
            case "name":
                return warp.name();
            case "owner":
                return warp.ownerName();
            case "visits":
                return Formats.number(warp.visits());
            case "rating":
                return Formats.decimal(warp.rating());
            case "stars":
                return CC.strip(plugin.warps().stars(warp.rating()));
            case "votes":
                return String.valueOf(warp.ratingCount());
            case "favorites":
                return String.valueOf(plugin.warps().favorites(warp.id()));
            case "level":
                return String.valueOf(warp.level());
            case "category":
                return CC.strip(plugin.categories().display(warp.category()));
            default:
                return "";
        }
    }
}
