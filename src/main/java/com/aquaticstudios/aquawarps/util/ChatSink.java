package com.aquaticstudios.aquawarps.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class ChatSink {

    public interface Delivery {

        boolean send(Player player, Component component) throws Throwable;
    }

    private static final class Route {

        private final String name;
        private final Delivery delivery;

        private Route(String name, Delivery delivery) {
            this.name = name;
            this.delivery = delivery;
        }
    }

    private final List<Route> routes = new ArrayList<>();
    private volatile int active;

    public ChatSink() {
        add(paper());
        add(spigotJson());
        add(spigotNative());
    }

    private void add(Route route) {
        if (route != null) {
            routes.add(route);
        }
    }

    public String active() {
        return active < routes.size() ? routes.get(active).name : "adventure";
    }

    public List<String> available() {
        List<String> names = new ArrayList<>();
        for (Route route : routes) {
            names.add(route.name);
        }
        names.add("adventure");
        return names;
    }

    public boolean send(Player player, Component component) {
        while (active < routes.size()) {
            Route route = routes.get(active);
            try {
                if (route.delivery.send(player, component)) {
                    return true;
                }
            } catch (Throwable ignored) {
                active++;
                continue;
            }
            active++;
        }
        return false;
    }

    private static Route paper() {
        try {
            String base = String.join(".", "net", "kyori", "adventure", "text");
            Class<?> componentType = Class.forName(base + ".Component");
            Class<?> gsonType = Class.forName(base + ".serializer.gson.GsonComponentSerializer");
            Object serializer = gsonType.getMethod("gson").invoke(null);

            Method found = null;
            for (Method method : gsonType.getMethods()) {
                if (method.getName().equals("deserialize") && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isAssignableFrom(String.class)) {
                    found = method;
                    break;
                }
            }
            if (found == null) {
                return null;
            }
            Method sendMessage = Player.class.getMethod("sendMessage", componentType);
            Method reader = found;

            return new Route("paper-native", (player, component) -> {
                Object parsed = reader.invoke(serializer,
                        GsonComponentSerializer.gson().serialize(component));
                if (parsed == null) {
                    return false;
                }
                sendMessage.invoke(player, parsed);
                return true;
            });
        } catch (Throwable throwable) {
            return null;
        }
    }

    private static Route spigotJson() {
        try {
            Class.forName("net.md_5.bungee.chat.ComponentSerializer");
            return new Route("spigot-json", (player, component) -> {
                BaseComponent[] parts = ComponentSerializer.parse(
                        GsonComponentSerializer.gson().serialize(component));
                if (parts == null || parts.length == 0) {
                    return false;
                }
                player.spigot().sendMessage(parts);
                return true;
            });
        } catch (Throwable throwable) {
            return null;
        }
    }

    private static Route spigotNative() {
        try {
            Class.forName("net.md_5.bungee.chat.ComponentSerializer");
            BungeeComponentSerializer serializer = BungeeComponentSerializer.get();
            if (serializer == null) {
                return null;
            }
            return new Route("spigot-components", (player, component) -> {
                BaseComponent[] parts = serializer.serialize(component);
                if (parts == null || parts.length == 0) {
                    return false;
                }
                player.spigot().sendMessage(parts);
                return true;
            });
        } catch (Throwable throwable) {
            return null;
        }
    }
}
