package com.aquaticstudios.aquawarps.model;

import java.util.List;

public final class Category {

    private final String id;
    private final String display;
    private final String material;
    private final int slot;
    private final int priority;
    private final String permission;
    private final List<String> description;
    private final boolean fallback;

    public Category(String id, String display, String material, int slot, int priority,
                    String permission, List<String> description, boolean fallback) {
        this.id = id;
        this.display = display;
        this.material = material;
        this.slot = slot;
        this.priority = priority;
        this.permission = permission;
        this.description = description;
        this.fallback = fallback;
    }

    public String id() {
        return id;
    }

    public String display() {
        return display;
    }

    public String material() {
        return material;
    }

    public int slot() {
        return slot;
    }

    public int priority() {
        return priority;
    }

    public String permission() {
        return permission;
    }

    public List<String> description() {
        return description;
    }

    public boolean fallback() {
        return fallback;
    }
}
