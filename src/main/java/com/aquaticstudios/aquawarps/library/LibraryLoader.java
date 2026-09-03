package com.aquaticstudios.aquawarps.library;

import com.aquaticstudios.aquawarps.database.DatabaseType;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.repository.Repository;

import java.io.File;
import java.net.URLClassLoader;
import java.util.logging.Level;

public final class LibraryLoader {

    public static void load(JavaPlugin plugin, DatabaseType type) {
        boolean needed = false;
        for (Libraries library : Libraries.values()) {
            if (library.isNeededFor(type)) {
                needed = true;
                break;
            }
        }
        if (!needed) {
            return;
        }

        ClassLoader classLoader = plugin.getClass().getClassLoader();
        if (!(classLoader instanceof URLClassLoader)) {
            plugin.getLogger().severe("Cannot load runtime libraries: unsupported class loader.");
            return;
        }

        try {
            File folder = new File(plugin.getDataFolder(), "libraries");
            DependencyManager manager = new DependencyManager(folder,
                    URLClassLoaderWrapper.wrap((URLClassLoader) classLoader));
            manager.repository(Repository.mavenCentral());

            for (Libraries library : Libraries.values()) {
                if (library.isNeededFor(type)) {
                    manager.dependency(library.dependency());
                }
            }

            plugin.getLogger().info("Downloading runtime libraries for " + type + " storage...");
            manager.load();
            plugin.getLogger().info("Runtime libraries ready.");
        } catch (Throwable throwable) {
            plugin.getLogger().log(Level.SEVERE,
                    "Failed to load runtime libraries for " + type + " storage.", throwable);
        }
    }
}
