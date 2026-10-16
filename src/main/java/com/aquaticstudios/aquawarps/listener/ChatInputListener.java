package com.aquaticstudios.aquawarps.listener;

import com.aquaticstudios.aquawarps.AquaWarps;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class ChatInputListener implements Listener {

    private final AquaWarps plugin;

    public ChatInputListener(AquaWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.chatInput().waiting(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        plugin.scheduler().global(() -> plugin.chatInput().submit(player, message));
    }
}
