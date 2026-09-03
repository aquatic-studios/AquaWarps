package com.aquaticstudios.aquawarps;

import com.aquaticstudios.aquawarps.command.WarpCommand;
import com.aquaticstudios.aquawarps.database.DatabaseType;
import com.aquaticstudios.aquawarps.database.StorageProvider;
import com.aquaticstudios.aquawarps.database.type.H2;
import com.aquaticstudios.aquawarps.database.type.MariaDB;
import com.aquaticstudios.aquawarps.database.type.MySQL;
import com.aquaticstudios.aquawarps.database.type.SQLite;
import com.aquaticstudios.aquawarps.hook.VaultHook;
import com.aquaticstudios.aquawarps.library.LibraryLoader;
import com.aquaticstudios.aquawarps.listener.ChatInputListener;
import com.aquaticstudios.aquawarps.listener.ConnectionListener;
import com.aquaticstudios.aquawarps.listener.MenuListener;
import com.aquaticstudios.aquawarps.manager.CategoryManager;
import com.aquaticstudios.aquawarps.manager.ChatInputManager;
import com.aquaticstudios.aquawarps.manager.PassportService;
import com.aquaticstudios.aquawarps.manager.ProfileManager;
import com.aquaticstudios.aquawarps.manager.PromotionService;
import com.aquaticstudios.aquawarps.manager.TeleportManager;
import com.aquaticstudios.aquawarps.manager.WarpManager;
import com.aquaticstudios.aquawarps.menu.MenuRegistry;
import com.aquaticstudios.aquawarps.placeholder.WarpExpansion;
import com.aquaticstudios.aquawarps.util.Cooldowns;
import com.aquaticstudios.aquawarps.util.NameCache;
import com.aquaticstudios.aquawarps.util.Scheduler;
import com.aquaticstudios.aquawarps.util.Settings;
import com.aquaticstudios.aquawarps.util.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class AquaWarps extends JavaPlugin {

    private static final String[] MENUS = {
            "admin", "all_warps", "browse", "categories", "favorites", "main", "my_warps",
            "newest", "passport", "reports", "sort", "stamps", "top_favorites", "top_level",
            "top_rated", "top_visits", "tops", "trending", "warp_bans", "warp_delete", "warp_edit",
            "warp_icons", "warp_info", "warp_particles", "warp_promote", "warp_rate",
            "warp_report", "warp_reviews", "warp_trusted"
    };

    private volatile Settings settings;
    private Scheduler scheduler;
    private MessageService messages;
    private NameCache names;
    private final Cooldowns cooldowns = new Cooldowns();

    private StorageProvider storage;
    private CategoryManager categories;
    private ProfileManager profiles;
    private WarpManager warps;
    private TeleportManager teleports;
    private ChatInputManager chatInput;
    private PromotionService promotions;
    private PassportService passport;
    private MenuRegistry menus;
    private VaultHook economy;
    private WarpCommand command;

    @Override
    public void onLoad() {
        DatabaseType type = DatabaseType.fromString(getConfig().getString("storage.type", "SQLITE"));
        LibraryLoader.load(this, type);
    }

    @Override
    public void onEnable() {
        saveDefaultResources();
        reloadSettings();

        this.scheduler = new Scheduler(this);
        this.messages = new MessageService(this);
        this.names = new NameCache();
        this.scheduler.async(names::warm);

        this.categories = new CategoryManager(this);
        this.profiles = new ProfileManager(this);
        this.warps = new WarpManager(this);
        this.teleports = new TeleportManager(this);
        this.chatInput = new ChatInputManager(this);
        this.promotions = new PromotionService(this);
        this.passport = new PassportService(this);
        this.economy = new VaultHook(this);
        this.menus = new MenuRegistry(this);

        this.storage = storageFor(settings.storageType());
        storage.init()
                .thenCompose(ignored -> warps.loadAll())
                .thenRun(() -> scheduler.global(warps::startTasks))
                .exceptionally(throwable -> {
                    getLogger().severe("Storage failed to initialise: " + throwable.getMessage());
                    return null;
                });

        profiles.startAutoSave();
        chatInput.start();
        registerListeners();

        this.command = new WarpCommand(this);
        Objects.requireNonNull(getCommand("aquawarps")).setExecutor(command);
        Objects.requireNonNull(getCommand("aquawarps")).setTabCompleter(command);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new WarpExpansion(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            names.store(online.getUniqueId(), online.getName());
            profiles.load(online);
        }

        getLogger().info("AquaWarps enabled.");
    }

    @Override
    public void onDisable() {
        if (chatInput != null) {
            chatInput.shutdown();
        }
        if (teleports != null) {
            teleports.shutdown();
        }
        if (warps != null) {
            warps.shutdown();
        }
        if (profiles != null) {
            profiles.shutdown();
        }
        if (storage != null) {
            storage.shutdown();
        }
        if (messages != null) {
            messages.shutdown();
        }
    }

    private void saveDefaultResources() {
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");
        saveResourceIfMissing("categories.yml");
        for (String menu : MENUS) {
            saveResourceIfMissing("menus/" + menu + ".yml");
        }
    }

    private void saveResourceIfMissing(String path) {
        if (!new File(getDataFolder(), path).exists()) {
            saveResource(path, false);
        }
    }

    public void reloadSettings() {
        reloadConfig();
        this.settings = new Settings(this);
    }

    public void reloadAll() {
        reloadSettings();
        messages.reload();
        categories.reload();
        menus.reload();
    }

    public StorageProvider storageFor(DatabaseType type) {
        switch (type) {
            case MYSQL:
                return new MySQL(this);
            case MARIADB:
                return new MariaDB(this);
            case H2:
                return new H2(this);
            case SQLITE:
            default:
                return new SQLite(this);
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatInputListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(teleports, this);
    }

    public Settings settings() {
        return settings;
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public MessageService messages() {
        return messages;
    }

    public NameCache names() {
        return names;
    }

    public Cooldowns cooldowns() {
        return cooldowns;
    }

    public StorageProvider storage() {
        return storage;
    }

    public CategoryManager categories() {
        return categories;
    }

    public ProfileManager profiles() {
        return profiles;
    }

    public WarpManager warps() {
        return warps;
    }

    public TeleportManager teleports() {
        return teleports;
    }

    public ChatInputManager chatInput() {
        return chatInput;
    }

    public PromotionService promotions() {
        return promotions;
    }

    public PassportService passport() {
        return passport;
    }

    public MenuRegistry menus() {
        return menus;
    }

    public VaultHook economy() {
        return economy;
    }

    public WarpCommand command() {
        return command;
    }
}
