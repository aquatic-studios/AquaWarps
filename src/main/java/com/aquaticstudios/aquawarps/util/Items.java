package com.aquaticstudios.aquawarps.util;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public final class Items {

    private final ItemStack item;
    private final ItemMeta meta;

    private Items(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    public static Items of(Material material) {
        return new Items(new ItemStack(material));
    }

    public static Items of(ItemStack item) {
        return new Items(item);
    }

    public static Items of(String material, Material fallback) {
        ItemStack custom = CustomItems.resolve(material);
        return custom != null ? new Items(custom) : of(material(material, fallback));
    }

    public static Material material(String name, Material fallback) {
        if (name == null || name.isEmpty()) {
            return fallback;
        }
        Material material = Material.matchMaterial(name.toUpperCase());
        return material == null || !material.isItem() ? fallback : material;
    }

    public Material type() {
        return item.getType();
    }

    public Items name(String name) {
        if (meta != null) {
            meta.setDisplayName(CC.toLegacy(name));
        }
        return this;
    }

    public Items lore(List<String> lines) {
        if (meta != null) {
            List<String> lore = new ArrayList<>(lines.size());
            for (String line : lines) {
                lore.add(CC.toLegacy(line));
            }
            meta.setLore(lore);
        }
        return this;
    }

    public Items amount(int amount) {
        item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    public Items owner(OfflinePlayer owner) {
        if (meta instanceof SkullMeta && owner != null && (owner.isOnline() || owner.getName() != null)) {
            ((SkullMeta) meta).setOwningPlayer(owner);
        }
        return this;
    }

    public Items glow(boolean glow) {
        if (meta != null && glow) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        }
        return this;
    }

    public Items modelData(int data) {
        if (meta != null && data > 0) {
            meta.setCustomModelData(data);
        }
        return this;
    }

    public Items hideAttributes() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
