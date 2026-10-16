package com.aquaticstudios.aquawarps.util;

import com.aquaticstudios.aquawarps.AquaWarps;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MessageService {

    private final AquaWarps plugin;
    private final BukkitAudiences audiences;
    private final ChatSink sink = new ChatSink();
    private FileConfiguration messages;
    private String prefix = "";

    public MessageService(AquaWarps plugin) {
        this.plugin = plugin;
        this.audiences = BukkitAudiences.create(plugin);
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(file);
        this.prefix = messages.getString("prefix", "");
    }

    public void shutdown() {
        audiences.close();
    }

    public BukkitAudiences audiences() {
        return audiences;
    }

    public ChatSink sink() {
        return sink;
    }

    private void deliver(CommandSender target, Component component) {
        if (target instanceof Player && sink.send((Player) target, component)) {
            return;
        }
        audiences.sender(target).sendMessage(component);
    }

    public String prefix() {
        return prefix;
    }

    public boolean has(String path) {
        return messages.contains(path);
    }

    public String raw(String path) {
        String value = messages.getString(path);
        return value == null ? "<red>Missing message: " + path : value;
    }

    public List<String> rawList(String path) {
        return messages.getStringList(path);
    }

    public String format(String path, String... placeholders) {
        String value = raw(path).replace("%prefix%", prefix);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            value = value.replace(placeholders[i], placeholders[i + 1]);
        }
        return value;
    }

    public void send(CommandSender target, String path, String... placeholders) {
        String value = format(path, placeholders);
        if (value.isEmpty()) {
            return;
        }
        deliver(target, CC.parse(papi(target, value)));
    }

    public void sendLine(CommandSender target, String line) {
        deliver(target, CC.parse(papi(target, line.replace("%prefix%", prefix))));
    }

    public void sendList(CommandSender target, String path, String... placeholders) {
        for (String line : rawList(path)) {
            String value = line.replace("%prefix%", prefix);
            for (int i = 0; i + 1 < placeholders.length; i += 2) {
                value = value.replace(placeholders[i], placeholders[i + 1]);
            }
            deliver(target, CC.parse(papi(target, value)));
        }
    }

    public void broadcast(String path, String... placeholders) {
        String value = format(path, placeholders);
        if (value.isEmpty()) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            deliver(online, CC.parse(papi(online, value)));
        }
    }

    public void staff(String permission, String path, String... placeholders) {
        String value = format(path, placeholders);
        if (value.isEmpty()) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(permission)) {
                deliver(online, CC.parse(papi(online, value)));
            }
        }
    }

    public void actionBar(Player player, String path, String... placeholders) {
        String value = format(path, placeholders);
        if (value.isEmpty()) {
            return;
        }
        audiences.player(player).sendActionBar(CC.parse(papi(player, value)));
    }

    public List<Component> lore(String path, Map<String, String> placeholders) {
        List<Component> lines = new ArrayList<>();
        for (String line : messages.getStringList(path)) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                line = line.replace(entry.getKey(), entry.getValue());
            }
            lines.add(CC.parse(line));
        }
        return lines;
    }

    private String papi(CommandSender target, String value) {
        if (target instanceof Player && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders((Player) target, value);
        }
        return value;
    }
}
