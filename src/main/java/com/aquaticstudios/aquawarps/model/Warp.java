package com.aquaticstudios.aquawarps.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Warp {

    private final String id;

    private UUID owner;
    private String ownerName;
    private String name;
    private String description = "";
    private String category;

    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    private String icon = "GRASS_BLOCK";
    private String particle = "NONE";

    private WarpAccess access = WarpAccess.PUBLIC;
    private String password;
    private double fee;
    private boolean open = true;
    private boolean hidden;
    private boolean featured;

    private long visits;
    private long ratingSum;
    private long ratingCount;
    private long experience;
    private int level = 1;

    private final Set<String> tags = new LinkedHashSet<>();
    private final Set<UUID> banned = ConcurrentHashMap.newKeySet();
    private final Set<UUID> trusted = ConcurrentHashMap.newKeySet();

    private long created;
    private long updated;
    private long promotedUntil;
    private String promotionTier;

    private double pearlsToday;
    private long pearlsDay;

    private volatile boolean dirty;

    public Warp(String id, UUID owner, String ownerName, String name) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.name = name;
        this.created = System.currentTimeMillis();
        this.updated = this.created;
    }

    public String id() {
        return id;
    }

    public UUID owner() {
        return owner;
    }

    public void owner(UUID owner) {
        this.owner = owner;
        markDirty();
    }

    public String ownerName() {
        return ownerName == null ? "Unknown" : ownerName;
    }

    public void ownerName(String ownerName) {
        this.ownerName = ownerName;
        markDirty();
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
        markDirty();
    }

    public String key() {
        return name.toLowerCase(Locale.ROOT);
    }

    public String description() {
        return description == null ? "" : description;
    }

    public List<String> descriptionLines() {
        List<String> lines = new ArrayList<>();
        if (description == null || description.isEmpty()) {
            return lines;
        }
        Collections.addAll(lines, description.split("\\|"));
        return lines;
    }

    public void description(String description) {
        this.description = description;
        markDirty();
    }

    public void addDescriptionLine(String line) {
        this.description = description == null || description.isEmpty() ? line : description + "|" + line;
        markDirty();
    }

    public String category() {
        return category;
    }

    public void category(String category) {
        this.category = category;
        markDirty();
    }

    public void location(Location location) {
        this.world = location.getWorld() == null ? null : location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        markDirty();
    }

    public void rawLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location location() {
        if (world == null) {
            return null;
        }
        World bukkitWorld = Bukkit.getWorld(world);
        return bukkitWorld == null ? null : new Location(bukkitWorld, x, y, z, yaw, pitch);
    }

    public boolean worldLoaded() {
        return world != null && Bukkit.getWorld(world) != null;
    }

    public String world() {
        return world == null ? "Unknown" : world;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public String icon() {
        return icon;
    }

    public void icon(String icon) {
        this.icon = icon;
        markDirty();
    }

    public Material iconMaterial(Material fallback) {
        if (icon == null) {
            return fallback;
        }
        Material material = Material.matchMaterial(icon.toUpperCase(Locale.ROOT));
        return material == null || !material.isItem() ? fallback : material;
    }

    public String particle() {
        return particle == null ? "NONE" : particle;
    }

    public void particle(String particle) {
        this.particle = particle;
        markDirty();
    }

    public WarpAccess access() {
        return access;
    }

    public void access(WarpAccess access) {
        this.access = access;
        markDirty();
    }

    public String password() {
        return password;
    }

    public boolean hasPassword() {
        return password != null && !password.isEmpty();
    }

    public void password(String password) {
        this.password = password;
        markDirty();
    }

    public double fee() {
        return fee;
    }

    public void fee(double fee) {
        this.fee = Math.max(0d, fee);
        markDirty();
    }

    public boolean open() {
        return open;
    }

    public void open(boolean open) {
        this.open = open;
        markDirty();
    }

    public boolean hidden() {
        return hidden;
    }

    public void hidden(boolean hidden) {
        this.hidden = hidden;
        markDirty();
    }

    public boolean featured() {
        return featured;
    }

    public void featured(boolean featured) {
        this.featured = featured;
        markDirty();
    }

    public boolean listable() {
        return !hidden && open && access != WarpAccess.PRIVATE;
    }

    public long visits() {
        return visits;
    }

    public void visits(long visits) {
        this.visits = visits;
    }

    public void addVisit() {
        this.visits++;
        markDirty();
    }

    public long ratingSum() {
        return ratingSum;
    }

    public long ratingCount() {
        return ratingCount;
    }

    public void ratings(long sum, long count) {
        this.ratingSum = sum;
        this.ratingCount = count;
    }

    public void applyRating(int oldStars, int newStars) {
        if (oldStars > 0) {
            ratingSum -= oldStars;
            ratingCount--;
        }
        if (newStars > 0) {
            ratingSum += newStars;
            ratingCount++;
        }
        if (ratingCount < 0L) {
            ratingCount = 0L;
        }
        if (ratingSum < 0L) {
            ratingSum = 0L;
        }
        markDirty();
    }

    public double rating() {
        return ratingCount == 0L ? 0d : (double) ratingSum / (double) ratingCount;
    }

    public long experience() {
        return experience;
    }

    public void experience(long experience) {
        this.experience = experience;
    }

    public void addExperience(long amount) {
        this.experience += amount;
        markDirty();
    }

    public int level() {
        return level;
    }

    public void level(int level) {
        this.level = level;
        markDirty();
    }

    public Set<String> tags() {
        return Collections.unmodifiableSet(tags);
    }

    public boolean addTag(String tag) {
        boolean added = tags.add(tag.toLowerCase(Locale.ROOT));
        if (added) {
            markDirty();
        }
        return added;
    }

    public boolean removeTag(String tag) {
        boolean removed = tags.remove(tag.toLowerCase(Locale.ROOT));
        if (removed) {
            markDirty();
        }
        return removed;
    }

    public String lastTag() {
        String last = null;
        for (String tag : tags) {
            last = tag;
        }
        return last;
    }

    public void tags(Iterable<String> values) {
        tags.clear();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isEmpty()) {
                    tags.add(value.toLowerCase(Locale.ROOT));
                }
            }
        }
    }

    public Set<UUID> banned() {
        return banned;
    }

    public Set<UUID> trusted() {
        return trusted;
    }

    public boolean isBanned(UUID uuid) {
        return banned.contains(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return owner.equals(uuid) || trusted.contains(uuid);
    }

    public long created() {
        return created;
    }

    public void created(long created) {
        this.created = created;
    }

    public long updated() {
        return updated;
    }

    public void updated(long updated) {
        this.updated = updated;
    }

    public long promotedUntil() {
        return promotedUntil;
    }

    public void promotedUntil(long promotedUntil) {
        this.promotedUntil = promotedUntil;
        markDirty();
    }

    public String promotionTier() {
        return promotionTier;
    }

    public void promotionTier(String promotionTier) {
        this.promotionTier = promotionTier;
        markDirty();
    }

    public boolean promoted() {
        return promotedUntil > System.currentTimeMillis();
    }

    public long promotionRemaining() {
        return Math.max(0L, promotedUntil - System.currentTimeMillis());
    }

    public void clearPromotion() {
        this.promotedUntil = 0L;
        this.promotionTier = null;
        markDirty();
    }

    public double pearlsToday() {
        rollDay();
        return pearlsToday;
    }

    public void addPearlsToday(double amount) {
        rollDay();
        pearlsToday += amount;
    }

    private void rollDay() {
        long today = System.currentTimeMillis() / 86_400_000L;
        if (pearlsDay != today) {
            pearlsDay = today;
            pearlsToday = 0d;
        }
    }

    public boolean dirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
        this.updated = System.currentTimeMillis();
    }

    public void markClean() {
        this.dirty = false;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Warp && ((Warp) other).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
