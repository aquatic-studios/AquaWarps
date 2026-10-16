package com.aquaticstudios.aquawarps.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerData {

    private final UUID uuid;
    private String name;

    private double pearls;
    private int extraSlots;
    private long lastSeen;

    private final Set<String> favorites = ConcurrentHashMap.newKeySet();
    private final Set<String> stamps = ConcurrentHashMap.newKeySet();
    private final Set<Integer> milestones = ConcurrentHashMap.newKeySet();
    private final Set<String> recent = Collections.synchronizedSet(new LinkedHashSet<>());

    private SortMode sort = SortMode.TRENDING;
    private String categoryFilter;
    private String searchQuery;

    private volatile boolean dirty;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.lastSeen = System.currentTimeMillis();
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name == null ? "Unknown" : name;
    }

    public void name(String name) {
        if (name != null && !name.equals(this.name)) {
            this.name = name;
            markDirty();
        }
    }

    public double pearls() {
        return pearls;
    }

    public void pearls(double pearls) {
        this.pearls = Math.max(0d, pearls);
        markDirty();
    }

    public void addPearls(double amount) {
        pearls(this.pearls + amount);
    }

    public boolean hasPearls(double amount) {
        return pearls >= amount;
    }

    public boolean takePearls(double amount) {
        if (!hasPearls(amount)) {
            return false;
        }
        pearls(this.pearls - amount);
        return true;
    }

    public int extraSlots() {
        return extraSlots;
    }

    public void extraSlots(int extraSlots) {
        this.extraSlots = Math.max(0, extraSlots);
        markDirty();
    }

    public void addExtraSlot() {
        extraSlots(extraSlots + 1);
    }

    public Set<String> favorites() {
        return favorites;
    }

    public boolean isFavorite(String warpId) {
        return favorites.contains(warpId);
    }

    public boolean toggleFavorite(String warpId) {
        boolean added;
        if (favorites.contains(warpId)) {
            favorites.remove(warpId);
            added = false;
        } else {
            favorites.add(warpId);
            added = true;
        }
        markDirty();
        return added;
    }

    public Set<String> stamps() {
        return stamps;
    }

    public int stampCount() {
        return stamps.size();
    }

    public boolean hasStamp(String warpId) {
        return stamps.contains(warpId);
    }

    public boolean addStamp(String warpId) {
        if (!stamps.add(warpId)) {
            return false;
        }
        synchronized (recent) {
            recent.remove(warpId);
            recent.add(warpId);
            while (recent.size() > 10) {
                java.util.Iterator<String> iterator = recent.iterator();
                iterator.next();
                iterator.remove();
            }
        }
        markDirty();
        return true;
    }

    public Set<String> recentStamps() {
        synchronized (recent) {
            return new LinkedHashSet<>(recent);
        }
    }

    public void recentStamps(Iterable<String> values) {
        synchronized (recent) {
            recent.clear();
            if (values != null) {
                for (String value : values) {
                    recent.add(value);
                }
            }
        }
    }

    public Set<Integer> milestones() {
        return milestones;
    }

    public boolean claimMilestone(int amount) {
        boolean claimed = milestones.add(amount);
        if (claimed) {
            markDirty();
        }
        return claimed;
    }

    public SortMode sort() {
        return sort;
    }

    public void sort(SortMode sort) {
        this.sort = sort;
    }

    public String categoryFilter() {
        return categoryFilter;
    }

    public void categoryFilter(String categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    public String searchQuery() {
        return searchQuery;
    }

    public void searchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public long lastSeen() {
        return lastSeen;
    }

    public void lastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
        markDirty();
    }

    public void touch() {
        lastSeen(System.currentTimeMillis());
    }

    public boolean dirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void markClean() {
        this.dirty = false;
    }
}
