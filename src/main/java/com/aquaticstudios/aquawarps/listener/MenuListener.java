package com.aquaticstudios.aquawarps.listener;

import com.aquaticstudios.aquawarps.AquaWarps;
import com.aquaticstudios.aquawarps.menu.ClickableMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public final class MenuListener implements Listener {

    private final AquaWarps plugin;

    public MenuListener(AquaWarps plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof ClickableMenu)) {
            return;
        }
        try {
            ((ClickableMenu) holder).handleClick(event);
        } catch (Exception exception) {
            event.setCancelled(true);
            plugin.getLogger().warning("Menu click failed: " + exception.getMessage());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ClickableMenu) {
            event.setCancelled(true);
        }
    }
}
