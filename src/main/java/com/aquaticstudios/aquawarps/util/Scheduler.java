package com.aquaticstudios.aquawarps.util;

import com.aquaticstudios.aquawarps.AquaWarps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class Scheduler {

    public interface Task {
        void cancel();
    }

    private final AquaWarps plugin;
    private final boolean folia;

    private Object globalScheduler;
    private Object asyncScheduler;
    private Method globalRun;
    private Method globalDelayed;
    private Method globalFixedRate;
    private Method asyncRunNow;
    private Method entityGetScheduler;
    private Method entityRunDelayed;
    private Method taskCancel;
    private Method teleportAsync;

    public Scheduler(AquaWarps plugin) {
        this.plugin = plugin;
        this.folia = detectFolia();
        if (folia) {
            setup();
        }
    }

    public boolean isFolia() {
        return folia;
    }

    public void global(Runnable task) {
        if (folia) {
            invoke(globalRun, globalScheduler, plugin, wrap(task));
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public Task globalDelayed(Runnable task, long delayTicks) {
        long delay = Math.max(1L, delayTicks);
        if (folia) {
            Object handle = invoke(globalDelayed, globalScheduler, plugin, wrap(task), delay);
            return () -> invoke(taskCancel, handle);
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        return bukkitTask::cancel;
    }

    public Task globalTimer(Runnable task, long delayTicks, long periodTicks) {
        if (folia) {
            Object handle = invoke(globalFixedRate, globalScheduler, plugin, wrap(task), delayTicks, periodTicks);
            return () -> invoke(taskCancel, handle);
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        return bukkitTask::cancel;
    }

    public void async(Runnable task) {
        if (folia) {
            invoke(asyncRunNow, asyncScheduler, plugin, wrap(task));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public Task entityDelayed(Entity entity, Runnable task, Runnable retired, long delayTicks) {
        if (folia) {
            Object entityScheduler = invoke(entityGetScheduler, entity);
            Object handle = invoke(entityRunDelayed, entityScheduler, plugin, wrap(task), retired, delayTicks);
            if (handle == null) {
                return () -> {
                };
            }
            return () -> invoke(taskCancel, handle);
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        return bukkitTask::cancel;
    }

    public void teleport(Player player, Location destination) {
        if (folia) {
            invoke(teleportAsync, player, destination);
        } else {
            player.teleport(destination);
        }
    }

    private Consumer<Object> wrap(Runnable task) {
        return ignored -> task.run();
    }

    private boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private void setup() {
        try {
            Class<?> globalClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Class<?> asyncClass = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            Class<?> entityClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Class<?> taskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            globalRun = globalClass.getMethod("run", Plugin.class, Consumer.class);
            globalDelayed = globalClass.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
            globalFixedRate = globalClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            asyncRunNow = asyncClass.getMethod("runNow", Plugin.class, Consumer.class);
            entityGetScheduler = Entity.class.getMethod("getScheduler");
            entityRunDelayed = entityClass.getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
            taskCancel = taskClass.getMethod("cancel");
            teleportAsync = Entity.class.getMethod("teleportAsync", Location.class);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to initialise Folia scheduler", ex);
        }
    }

    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Scheduler call failed: " + method.getName(), ex);
        }
    }
}
