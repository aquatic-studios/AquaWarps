package com.aquaticstudios.aquawarps.listener;

import com.aquaticstudios.aquawarps.AquaWarps;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ConnectionListener implements Listener {

    private final AquaWarps plugin;

    public ConnectionListener(AquaWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.names().store(player.getUniqueId(), player.getName());
        plugin.profiles().load(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.chatInput().clear(player.getUniqueId());
        plugin.cooldowns().clear(player.getUniqueId());
        plugin.profiles().unload(player);
    }
}
