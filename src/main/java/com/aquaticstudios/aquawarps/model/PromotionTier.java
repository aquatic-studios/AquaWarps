package com.aquaticstudios.aquawarps.model;

public final class PromotionTier {

    private final String id;
    private final String display;
    private final String material;
    private final int priority;
    private final long duration;
    private final double price;
    private final boolean glow;

    public PromotionTier(String id, String display, String material, int priority,
                         long duration, double price, boolean glow) {
        this.id = id;
        this.display = display;
        this.material = material;
        this.priority = priority;
        this.duration = duration;
        this.price = price;
        this.glow = glow;
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

    public int priority() {
        return priority;
    }

    public long duration() {
        return duration;
    }

    public double price() {
        return price;
    }

    public boolean glow() {
        return glow;
    }
}
