package com.aquaticstudios.aquawarps.util;

import com.aquaticstudios.aquawarps.AquaWarps;
import com.aquaticstudios.aquawarps.database.DatabaseType;
import com.aquaticstudios.aquawarps.model.Milestone;
import com.aquaticstudios.aquawarps.model.PromotionTier;
import com.aquaticstudios.aquawarps.model.SortMode;
import com.aquaticstudios.aquawarps.model.WarpPerk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public final class Settings {

    private final DatabaseType storageType;
    private final String mysqlHost;
    private final int mysqlPort;
    private final String mysqlDatabase;
    private final String mysqlUsername;
    private final String mysqlPassword;
    private final boolean mysqlSsl;

    private final int nameMinLength;
    private final int nameMaxLength;
    private final String namePattern;
    private final List<String> nameBlacklist;
    private final int descriptionMaxLines;
    private final int descriptionMaxLength;
    private final boolean tagsEnabled;
    private final int tagsMax;
    private final int tagsMaxLength;
    private final int limitDefault;
    private final Map<String, Integer> limitPermissions;
    private final List<String> blockedWorlds;
    private final double minDistance;
    private final boolean safeTeleport;
    private final boolean requireOwnerOnline;
    private final int deleteAfterInactiveDays;

    private final int warmupSeconds;
    private final boolean cancelOnMove;
    private final boolean cancelOnDamage;
    private final int teleportCooldown;
    private final boolean countdownActionBar;
    private final boolean countdownTitle;
    private final String warmupParticle;
    private final String warmupSound;
    private final String arriveParticle;
    private final String arriveSound;
    private final boolean backEnabled;

    private final boolean economyEnabled;
    private final String economyProvider;
    private final double createCost;
    private final double renameCost;
    private final double relocateCost;
    private final double extraSlotCost;
    private final double extraSlotMultiplier;
    private final int extraSlotMax;
    private final double feeMax;
    private final double feeTaxPercent;

    private final boolean pearlsEnabled;
    private final String pearlsName;
    private final double pearlsStarting;
    private final double rewardUniqueVisit;
    private final double rewardRating;
    private final double rewardFavorite;
    private final double rewardDiscovery;
    private final double pearlsDailyCap;

    private final boolean ratingEnabled;
    private final boolean ratingComments;
    private final int commentMaxLength;
    private final boolean requireVisit;
    private final boolean blockSelfRating;
    private final int ratingCooldownHours;
    private final int minimumForTop;
    private final int reviewsShown;

    private final boolean promotionEnabled;
    private final int promotionMaxActive;
    private final String promotionCurrency;
    private final Map<String, PromotionTier> promotionTiers;

    private final int trendWindowDays;
    private final double trendVisitWeight;
    private final double trendUniqueWeight;
    private final double trendRatingWeight;
    private final double trendFavoriteWeight;
    private final double trendDecay;
    private final int trendRecalculateMinutes;

    private final boolean levelEnabled;
    private final int maxLevel;
    private final double levelBase;
    private final double levelExponent;
    private final int xpVisit;
    private final int xpUniqueVisit;
    private final int xpRating;
    private final int xpFavorite;
    private final Map<Integer, List<WarpPerk>> perks;

    private final boolean passportEnabled;
    private final TreeMap<Integer, Milestone> milestones;
    private final TreeMap<Integer, String> ranks;

    private final boolean reportsEnabled;
    private final List<String> reportReasons;
    private final int autoHideAfter;
    private final boolean notifyStaff;
    private final int reportCooldownMinutes;

    private final SortMode defaultSort;
    private final boolean promotedFirst;
    private final boolean hidePrivate;
    private final boolean showWorld;
    private final boolean showCoordinates;
    private final int tagsInLore;

    private final boolean notifyOwnerVisit;
    private final boolean notifyOwnerRating;
    private final boolean broadcastNewWarps;
    private final boolean broadcastLevelUp;

    private final int inputTimeoutSeconds;
    private final List<String> inputCancelWords;
    private final boolean inputBossBar;
    private final String inputBossBarColor;
    private final String inputBossBarStyle;
    private final boolean inputTitle;
    private final String inputSoundOpen;
    private final String inputSoundSuccess;
    private final String inputSoundCancel;

    private final List<String> menuIcons;
    private final List<String> menuParticles;

    private final Map<String, String> noWarp;
    private final String starFull;
    private final String starEmpty;

    public Settings(AquaWarps plugin) {
        FileConfiguration config = plugin.getConfig();

        this.storageType = DatabaseType.fromString(config.getString("storage.type", "SQLITE"));
        this.mysqlHost = config.getString("storage.mysql.host", "localhost");
        this.mysqlPort = config.getInt("storage.mysql.port", 3306);
        this.mysqlDatabase = config.getString("storage.mysql.database", "aquawarps");
        this.mysqlUsername = config.getString("storage.mysql.username", "root");
        this.mysqlPassword = config.getString("storage.mysql.password", "");
        this.mysqlSsl = config.getBoolean("storage.mysql.ssl", false);

        this.nameMinLength = config.getInt("warp.name.min-length", 3);
        this.nameMaxLength = config.getInt("warp.name.max-length", 16);
        this.namePattern = config.getString("warp.name.pattern", "^[a-zA-Z0-9_]+$");
        this.nameBlacklist = lower(config.getStringList("warp.name.blacklist"));
        this.descriptionMaxLines = config.getInt("warp.description.max-lines", 4);
        this.descriptionMaxLength = config.getInt("warp.description.max-length", 64);
        this.tagsEnabled = config.getBoolean("warp.tags.enabled", true);
        this.tagsMax = config.getInt("warp.tags.max", 5);
        this.tagsMaxLength = config.getInt("warp.tags.max-length", 12);
        this.limitDefault = config.getInt("warp.limits.default", 3);
        this.limitPermissions = readLimits(config.getConfigurationSection("warp.limits.permissions"));
        this.blockedWorlds = lower(config.getStringList("warp.blocked-worlds"));
        this.minDistance = config.getDouble("warp.min-distance", 0d);
        this.safeTeleport = config.getBoolean("warp.safe-teleport", true);
        this.requireOwnerOnline = config.getBoolean("warp.require-owner-online", false);
        this.deleteAfterInactiveDays = config.getInt("warp.delete-after-inactive-days", 0);

        this.warmupSeconds = Math.max(0, config.getInt("teleport.warmup-seconds", 3));
        this.cancelOnMove = config.getBoolean("teleport.cancel-on-move", true);
        this.cancelOnDamage = config.getBoolean("teleport.cancel-on-damage", true);
        this.teleportCooldown = Math.max(0, config.getInt("teleport.cooldown-seconds", 10));
        this.countdownActionBar = config.getBoolean("teleport.countdown-actionbar", true);
        this.countdownTitle = config.getBoolean("teleport.countdown-title", true);
        this.warmupParticle = config.getString("teleport.warmup-particle", "WATER_WAKE");
        this.warmupSound = config.getString("teleport.warmup-sound", "BLOCK_NOTE_BLOCK_BIT");
        this.arriveParticle = config.getString("teleport.arrive-particle", "WATER_SPLASH");
        this.arriveSound = config.getString("teleport.arrive-sound", "ENTITY_DOLPHIN_SPLASH");
        this.backEnabled = config.getBoolean("teleport.enable-back", true);

        this.economyEnabled = config.getBoolean("economy.enabled", true);
        this.economyProvider = config.getString("economy.provider", "VAULT").toUpperCase(Locale.ROOT);
        this.createCost = config.getDouble("economy.create-cost", 0d);
        this.renameCost = config.getDouble("economy.rename-cost", 0d);
        this.relocateCost = config.getDouble("economy.relocate-cost", 0d);
        this.extraSlotCost = config.getDouble("economy.extra-slot.cost", 5000d);
        this.extraSlotMultiplier = config.getDouble("economy.extra-slot.multiplier", 1.75d);
        this.extraSlotMax = config.getInt("economy.extra-slot.max", 5);
        this.feeMax = config.getDouble("economy.fee.max", 5000d);
        this.feeTaxPercent = config.getDouble("economy.fee.tax-percent", 5d);

        this.pearlsEnabled = config.getBoolean("pearls.enabled", true);
        this.pearlsName = config.getString("pearls.name", "Pearls");
        this.pearlsStarting = config.getDouble("pearls.starting-amount", 50d);
        this.rewardUniqueVisit = config.getDouble("pearls.reward-unique-visit", 2d);
        this.rewardRating = config.getDouble("pearls.reward-rating", 5d);
        this.rewardFavorite = config.getDouble("pearls.reward-favorite", 3d);
        this.rewardDiscovery = config.getDouble("pearls.reward-visitor-discovery", 1d);
        this.pearlsDailyCap = config.getDouble("pearls.daily-cap", 100d);

        this.ratingEnabled = config.getBoolean("rating.enabled", true);
        this.ratingComments = config.getBoolean("rating.comments", true);
        this.commentMaxLength = config.getInt("rating.comment-max-length", 80);
        this.requireVisit = config.getBoolean("rating.require-visit", true);
        this.blockSelfRating = config.getBoolean("rating.block-self", true);
        this.ratingCooldownHours = config.getInt("rating.change-cooldown-hours", 24);
        this.minimumForTop = config.getInt("rating.minimum-for-top", 3);
        this.reviewsShown = Math.max(9, config.getInt("rating.reviews-shown", 45));

        this.promotionEnabled = config.getBoolean("promotion.enabled", true);
        this.promotionMaxActive = config.getInt("promotion.max-active", 12);
        this.promotionCurrency = config.getString("promotion.currency", "PEARLS").toUpperCase(Locale.ROOT);
        this.promotionTiers = readTiers(config.getConfigurationSection("promotion.tiers"));

        this.trendWindowDays = Math.max(1, config.getInt("trending.window-days", 7));
        this.trendVisitWeight = config.getDouble("trending.visit-weight", 1d);
        this.trendUniqueWeight = config.getDouble("trending.unique-weight", 2.5d);
        this.trendRatingWeight = config.getDouble("trending.rating-weight", 8d);
        this.trendFavoriteWeight = config.getDouble("trending.favorite-weight", 4d);
        this.trendDecay = config.getDouble("trending.decay-per-day", 0.82d);
        this.trendRecalculateMinutes = Math.max(1, config.getInt("trending.recalculate-minutes", 5));

        this.levelEnabled = config.getBoolean("level.enabled", true);
        this.maxLevel = Math.max(1, config.getInt("level.max-level", 25));
        this.levelBase = config.getDouble("level.base", 100d);
        this.levelExponent = config.getDouble("level.exponent", 1.45d);
        this.xpVisit = config.getInt("level.experience.visit", 3);
        this.xpUniqueVisit = config.getInt("level.experience.unique-visit", 10);
        this.xpRating = config.getInt("level.experience.rating", 25);
        this.xpFavorite = config.getInt("level.experience.favorite", 15);
        this.perks = readPerks(config.getConfigurationSection("level.perks"));

        this.passportEnabled = config.getBoolean("passport.enabled", true);
        this.milestones = readMilestones(config.getConfigurationSection("passport.milestones"));
        this.ranks = readRanks(config.getConfigurationSection("passport.ranks"));

        this.reportsEnabled = config.getBoolean("moderation.reports", true);
        List<String> reasons = config.getStringList("moderation.reasons");
        this.reportReasons = reasons.isEmpty() ? Collections.singletonList("Other") : reasons;
        this.autoHideAfter = config.getInt("moderation.auto-hide-after", 5);
        this.notifyStaff = config.getBoolean("moderation.notify-staff", true);
        this.reportCooldownMinutes = config.getInt("moderation.cooldown-minutes", 5);

        this.defaultSort = SortMode.fromString(config.getString("listing.default-sort"), SortMode.TRENDING);
        this.promotedFirst = config.getBoolean("listing.promoted-first", true);
        this.hidePrivate = config.getBoolean("listing.hide-private", true);
        this.showWorld = config.getBoolean("listing.show-world", true);
        this.showCoordinates = config.getBoolean("listing.show-coordinates", false);
        this.tagsInLore = config.getInt("listing.tags-in-lore", 3);

        this.notifyOwnerVisit = config.getBoolean("notifications.owner-on-visit", true);
        this.notifyOwnerRating = config.getBoolean("notifications.owner-on-rating", true);
        this.broadcastNewWarps = config.getBoolean("notifications.broadcast-new-warps", false);
        this.broadcastLevelUp = config.getBoolean("notifications.broadcast-level-up", true);

        this.inputTimeoutSeconds = Math.max(5, config.getInt("input.timeout-seconds", 60));
        List<String> cancelWords = lower(config.getStringList("input.cancel-words"));
        this.inputCancelWords = cancelWords.isEmpty() ? Collections.singletonList("cancel") : cancelWords;
        this.inputBossBar = config.getBoolean("input.bossbar", true);
        this.inputBossBarColor = config.getString("input.bossbar-color", "BLUE");
        this.inputBossBarStyle = config.getString("input.bossbar-style", "PROGRESS");
        this.inputTitle = config.getBoolean("input.title", true);
        this.inputSoundOpen = config.getString("input.sound-open", "BLOCK_NOTE_BLOCK_PLING");
        this.inputSoundSuccess = config.getString("input.sound-success", "ENTITY_PLAYER_LEVELUP");
        this.inputSoundCancel = config.getString("input.sound-cancel", "BLOCK_NOTE_BLOCK_BASS");

        this.menuIcons = config.getStringList("menu.icons");
        this.menuParticles = config.getStringList("menu.particles");

        Map<String, String> fallbacks = new LinkedHashMap<>();
        ConfigurationSection noWarpSection = config.getConfigurationSection("placeholders.no-warp");
        if (noWarpSection != null) {
            for (String key : noWarpSection.getKeys(false)) {
                fallbacks.put(key.toLowerCase(Locale.ROOT), noWarpSection.getString(key, ""));
            }
        }
        this.noWarp = fallbacks;
        this.starFull = config.getString("placeholders.stars.full", "&#FFEE00*");
        this.starEmpty = config.getString("placeholders.stars.empty", "&8*");
    }

    private static List<String> lower(List<String> values) {
        List<String> out = new ArrayList<>(values.size());
        for (String value : values) {
            out.add(value.toLowerCase(Locale.ROOT));
        }
        return out;
    }

    private static Map<String, Integer> readLimits(ConfigurationSection section) {
        Map<String, Integer> out = new LinkedHashMap<>();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                out.put(key.toLowerCase(Locale.ROOT), section.getInt(key));
            }
        }
        return out;
    }

    private static Map<String, PromotionTier> readTiers(ConfigurationSection section) {
        Map<String, PromotionTier> out = new LinkedHashMap<>();
        if (section == null) {
            return out;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection tier = section.getConfigurationSection(key);
            if (tier == null) {
                continue;
            }
            String id = key.toLowerCase(Locale.ROOT);
            out.put(id, new PromotionTier(
                    id,
                    tier.getString("display", key),
                    tier.getString("material", "NAUTILUS_SHELL"),
                    tier.getInt("priority", 1),
                    Math.max(1L, tier.getLong("duration-hours", 24L)) * 3_600_000L,
                    tier.getDouble("price", 100d),
                    tier.getBoolean("glow", false)));
        }
        return out;
    }

    private static Map<Integer, List<WarpPerk>> readPerks(ConfigurationSection section) {
        Map<Integer, List<WarpPerk>> out = new TreeMap<>();
        if (section == null) {
            return out;
        }
        for (String key : section.getKeys(false)) {
            int level = parseInt(key, -1);
            if (level < 0) {
                continue;
            }
            List<WarpPerk> list = new ArrayList<>();
            for (String raw : section.getStringList(key)) {
                WarpPerk perk = WarpPerk.fromString(raw);
                if (perk != null) {
                    list.add(perk);
                }
            }
            if (!list.isEmpty()) {
                out.put(level, list);
            }
        }
        return out;
    }

    private static TreeMap<Integer, Milestone> readMilestones(ConfigurationSection section) {
        TreeMap<Integer, Milestone> out = new TreeMap<>();
        if (section == null) {
            return out;
        }
        for (String key : section.getKeys(false)) {
            int amount = parseInt(key, -1);
            if (amount <= 0) {
                continue;
            }
            out.put(amount, new Milestone(amount,
                    section.getDouble(key + ".pearls", 0d),
                    section.getStringList(key + ".commands")));
        }
        return out;
    }

    private static TreeMap<Integer, String> readRanks(ConfigurationSection section) {
        TreeMap<Integer, String> out = new TreeMap<>();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int amount = parseInt(key, -1);
                if (amount >= 0) {
                    out.put(amount, section.getString(key, ""));
                }
            }
        }
        if (out.isEmpty()) {
            out.put(0, "&7Drifter");
        }
        return out;
    }

    private static int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public int permissionLimit(Player player) {
        int best = limitDefault;
        for (Map.Entry<String, Integer> entry : limitPermissions.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                best = Math.max(best, entry.getValue());
            }
        }
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) {
                continue;
            }
            String permission = info.getPermission().toLowerCase(Locale.ROOT);
            if (!permission.startsWith("aquawarps.limit.")) {
                continue;
            }
            String suffix = permission.substring("aquawarps.limit.".length());
            if ("*".equals(suffix)) {
                return Integer.MAX_VALUE;
            }
            int parsed = parseInt(suffix, -1);
            if (parsed > best) {
                best = parsed;
            }
        }
        return best;
    }

    public boolean hasPerk(int level, WarpPerk perk) {
        if (!levelEnabled) {
            return true;
        }
        for (Map.Entry<Integer, List<WarpPerk>> entry : perks.entrySet()) {
            if (entry.getKey() <= level && entry.getValue().contains(perk)) {
                return true;
            }
        }
        return false;
    }

    public int perkStacks(int level, WarpPerk perk) {
        int count = 0;
        for (Map.Entry<Integer, List<WarpPerk>> entry : perks.entrySet()) {
            if (entry.getKey() > level) {
                break;
            }
            for (WarpPerk value : entry.getValue()) {
                if (value == perk) {
                    count++;
                }
            }
        }
        return count;
    }

    public int perkUnlockLevel(WarpPerk perk) {
        for (Map.Entry<Integer, List<WarpPerk>> entry : perks.entrySet()) {
            if (entry.getValue().contains(perk)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public String rankFor(int stamps) {
        String rank = ranks.firstEntry().getValue();
        for (Map.Entry<Integer, String> entry : ranks.entrySet()) {
            if (stamps >= entry.getKey()) {
                rank = entry.getValue();
            } else {
                break;
            }
        }
        return rank;
    }

    public Map.Entry<Integer, String> nextRank(int stamps) {
        return ranks.higherEntry(stamps);
    }

    public PromotionTier tier(String id) {
        return id == null ? null : promotionTiers.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean promotionUsesPearls() {
        return "PEARLS".equals(promotionCurrency);
    }

    public boolean economyUsesPearls() {
        return "PEARLS".equals(economyProvider);
    }

    public String noWarp(String key) {
        return noWarp.getOrDefault(key, "");
    }

    public DatabaseType storageType() {
        return storageType;
    }

    public String mysqlHost() {
        return mysqlHost;
    }

    public int mysqlPort() {
        return mysqlPort;
    }

    public String mysqlDatabase() {
        return mysqlDatabase;
    }

    public String mysqlUsername() {
        return mysqlUsername;
    }

    public String mysqlPassword() {
        return mysqlPassword;
    }

    public boolean mysqlSsl() {
        return mysqlSsl;
    }

    public int limitDefaultValue() {
        return limitDefault;
    }

    public int nameMinLength() {
        return nameMinLength;
    }

    public int nameMaxLength() {
        return nameMaxLength;
    }

    public String namePattern() {
        return namePattern;
    }

    public List<String> nameBlacklist() {
        return nameBlacklist;
    }

    public int descriptionMaxLines() {
        return descriptionMaxLines;
    }

    public int descriptionMaxLength() {
        return descriptionMaxLength;
    }

    public boolean tagsEnabled() {
        return tagsEnabled;
    }

    public int tagsMax() {
        return tagsMax;
    }

    public int tagsMaxLength() {
        return tagsMaxLength;
    }

    public List<String> blockedWorlds() {
        return blockedWorlds;
    }

    public double minDistance() {
        return minDistance;
    }

    public boolean safeTeleport() {
        return safeTeleport;
    }

    public boolean requireOwnerOnline() {
        return requireOwnerOnline;
    }

    public int deleteAfterInactiveDays() {
        return deleteAfterInactiveDays;
    }

    public int warmupSeconds() {
        return warmupSeconds;
    }

    public boolean cancelOnMove() {
        return cancelOnMove;
    }

    public boolean cancelOnDamage() {
        return cancelOnDamage;
    }

    public int teleportCooldown() {
        return teleportCooldown;
    }

    public boolean countdownActionBar() {
        return countdownActionBar;
    }

    public boolean countdownTitle() {
        return countdownTitle;
    }

    public String warmupParticle() {
        return warmupParticle;
    }

    public String warmupSound() {
        return warmupSound;
    }

    public String arriveParticle() {
        return arriveParticle;
    }

    public String arriveSound() {
        return arriveSound;
    }

    public boolean backEnabled() {
        return backEnabled;
    }

    public boolean economyEnabled() {
        return economyEnabled;
    }

    public double createCost() {
        return createCost;
    }

    public double renameCost() {
        return renameCost;
    }

    public double relocateCost() {
        return relocateCost;
    }

    public double extraSlotCost() {
        return extraSlotCost;
    }

    public double extraSlotMultiplier() {
        return extraSlotMultiplier;
    }

    public int extraSlotMax() {
        return extraSlotMax;
    }

    public double feeMax() {
        return feeMax;
    }

    public double feeTaxPercent() {
        return feeTaxPercent;
    }

    public boolean pearlsEnabled() {
        return pearlsEnabled;
    }

    public String pearlsName() {
        return pearlsName;
    }

    public double pearlsStarting() {
        return pearlsStarting;
    }

    public double rewardUniqueVisit() {
        return rewardUniqueVisit;
    }

    public double rewardRating() {
        return rewardRating;
    }

    public double rewardFavorite() {
        return rewardFavorite;
    }

    public double rewardDiscovery() {
        return rewardDiscovery;
    }

    public double pearlsDailyCap() {
        return pearlsDailyCap;
    }

    public boolean ratingEnabled() {
        return ratingEnabled;
    }

    public boolean ratingComments() {
        return ratingComments;
    }

    public int commentMaxLength() {
        return commentMaxLength;
    }

    public boolean requireVisit() {
        return requireVisit;
    }

    public boolean blockSelfRating() {
        return blockSelfRating;
    }

    public int ratingCooldownHours() {
        return ratingCooldownHours;
    }

    public int minimumForTop() {
        return minimumForTop;
    }

    public int reviewsShown() {
        return reviewsShown;
    }

    public boolean promotionEnabled() {
        return promotionEnabled;
    }

    public int promotionMaxActive() {
        return promotionMaxActive;
    }

    public Map<String, PromotionTier> promotionTiers() {
        return promotionTiers;
    }

    public int trendWindowDays() {
        return trendWindowDays;
    }

    public double trendVisitWeight() {
        return trendVisitWeight;
    }

    public double trendUniqueWeight() {
        return trendUniqueWeight;
    }

    public double trendRatingWeight() {
        return trendRatingWeight;
    }

    public double trendFavoriteWeight() {
        return trendFavoriteWeight;
    }

    public double trendDecay() {
        return trendDecay;
    }

    public int trendRecalculateMinutes() {
        return trendRecalculateMinutes;
    }

    public boolean levelEnabled() {
        return levelEnabled;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public double levelBase() {
        return levelBase;
    }

    public double levelExponent() {
        return levelExponent;
    }

    public int xpVisit() {
        return xpVisit;
    }

    public int xpUniqueVisit() {
        return xpUniqueVisit;
    }

    public int xpRating() {
        return xpRating;
    }

    public int xpFavorite() {
        return xpFavorite;
    }

    public Map<Integer, List<WarpPerk>> perks() {
        return perks;
    }

    public boolean passportEnabled() {
        return passportEnabled;
    }

    public TreeMap<Integer, Milestone> milestones() {
        return milestones;
    }

    public TreeMap<Integer, String> ranks() {
        return ranks;
    }

    public boolean reportsEnabled() {
        return reportsEnabled;
    }

    public List<String> reportReasons() {
        return reportReasons;
    }

    public int autoHideAfter() {
        return autoHideAfter;
    }

    public boolean notifyStaff() {
        return notifyStaff;
    }

    public int reportCooldownMinutes() {
        return reportCooldownMinutes;
    }

    public SortMode defaultSort() {
        return defaultSort;
    }

    public boolean promotedFirst() {
        return promotedFirst;
    }

    public boolean hidePrivate() {
        return hidePrivate;
    }

    public boolean showWorld() {
        return showWorld;
    }

    public boolean showCoordinates() {
        return showCoordinates;
    }

    public int tagsInLore() {
        return tagsInLore;
    }

    public boolean notifyOwnerVisit() {
        return notifyOwnerVisit;
    }

    public boolean notifyOwnerRating() {
        return notifyOwnerRating;
    }

    public boolean broadcastNewWarps() {
        return broadcastNewWarps;
    }

    public boolean broadcastLevelUp() {
        return broadcastLevelUp;
    }

    public int inputTimeoutSeconds() {
        return inputTimeoutSeconds;
    }

    public List<String> inputCancelWords() {
        return inputCancelWords;
    }

    public boolean inputBossBar() {
        return inputBossBar;
    }

    public String inputBossBarColor() {
        return inputBossBarColor;
    }

    public String inputBossBarStyle() {
        return inputBossBarStyle;
    }

    public boolean inputTitle() {
        return inputTitle;
    }

    public String inputSoundOpen() {
        return inputSoundOpen;
    }

    public String inputSoundSuccess() {
        return inputSoundSuccess;
    }

    public String inputSoundCancel() {
        return inputSoundCancel;
    }

    public List<String> menuIcons() {
        return menuIcons;
    }

    public List<String> menuParticles() {
        return menuParticles;
    }

    public String starFull() {
        return starFull;
    }

    public String starEmpty() {
        return starEmpty;
    }
}
